package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.base.entity.ProposeTopic;
import com.zeroxn.bbs.base.entity.UserExtras;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.core.filter.SensitiveTextFilter;
import com.zeroxn.bbs.web.dto.*;
import com.zeroxn.bbs.web.mapper.ForumTopicMapper;
import com.zeroxn.bbs.web.mapper.ProposeTopicMapper;
import com.zeroxn.bbs.web.mapper.UserExtrasMapper;
import com.zeroxn.bbs.web.service.CommentService;
import com.zeroxn.bbs.web.service.ContentService;
import com.zeroxn.bbs.web.service.async.GlobalAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.count;
import static com.mybatisflex.core.query.QueryMethods.noCondition;
import static com.zeroxn.bbs.base.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.base.entity.table.ForumTopicTableDef.FORUM_TOPIC;
import static com.zeroxn.bbs.base.entity.table.ProposeTopicTableDef.PROPOSE_TOPIC;
import static com.zeroxn.bbs.base.entity.table.UserExtrasTableDef.USER_EXTRAS;
import static com.zeroxn.bbs.base.entity.table.UserTableDef.USER;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 11:35:14
 * @Description: 帖子/话题服务层实现类
 */
@Service
public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);
    /**
     * 用户发帖的频率限制时间
     */
    private static final int PUBLISH_TOPIC_LIMIT_DATE_HOUR = 1;
    private final CommentService commentService;
    private final ForumTopicMapper topicMapper;
    private final UserExtrasMapper extrasMapper;
    private final ProposeTopicMapper proposeMapper;
    private final GlobalAsyncTask asyncTask;
    private final SensitiveTextFilter textFilter;
    private final RabbitTemplate rabbitTemplate;

    public ContentServiceImpl(ForumTopicMapper topicMapper, GlobalAsyncTask asyncTask, UserExtrasMapper extrasMapper,
                              ProposeTopicMapper proposeMapper, RabbitTemplate rabbitTemplate, CommentService commentService,
                              SensitiveTextFilter textFilter) {
        this.topicMapper = topicMapper;
        this.asyncTask = asyncTask;
        this.extrasMapper = extrasMapper;
        this.proposeMapper = proposeMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.commentService = commentService;
        this.textFilter = textFilter;
    }

    /**
     * 话题发布先检查用户是否触发了限制，如果没有触发限制那么在保存成功后调用消息队列发送消息 生成关键字
     * @param topic 话题信息
     */
    @Override
    public void saveTopic(ForumTopic topic) {
        this.checkSensitiveWord(topic.getTitle() + "," + topic.getContent());
        this.checkTopicPublishTime(topic.getUserId(), false);
        topic.setType(1);
        topicMapper.insertSelective(topic);
        logger.info("话题保存成功，TopicId:{}", topic.getId());
        rabbitTemplate.convertAndSend("bbs.topic", topic);
        logger.info("RabbitMQ发送消息成功，TopicId:{}", topic.getId());
    }

    /**
     * 逻辑和保存话题一致
     * @param post 帖子信息
     */
    @Override
    public void savePost(ForumTopic post) {
        this.checkSensitiveWord(post.getTitle() + "," + post.getContent());
        this.checkTopicPublishTime(post.getUserId(), true);
        post.setType(0);
        topicMapper.insertSelective(post);
        logger.info("帖子保存成功，TopicId：{}", post.getId());
        rabbitTemplate.convertAndSend("bbs.topic", post);
        logger.info("RabbitMQ发送消息成功，TopicId:{}", post.getId());
    }

    /**
     * 查询帖子列表时需要同时查询帖子的评论数
     * @param postDto 查询帖子列表的查询参数
     * @return 返回帖子列表
     */
    @Override
    public Page<UserTopicDto> pageForumPost(QueryPostDto postDto) {
        Integer condition = postDto.getCondition();
        // 设置排序规则
        String sortColumn;
        if (condition != null && condition.equals(0)) {
            sortColumn = FORUM_TOPIC.VIEW_COUNT.getName();
        }else if (condition != null && condition.equals(1)) {
            sortColumn = "comment_count";
        }else {
            sortColumn = FORUM_TOPIC.CREATE_TIME.getName();
        }
        QueryWrapper queryWrapper = initUserTopicQueryWrapper(0)
                .and(postDto.getFlag() != null ? FORUM_TOPIC.FLAG.eq(postDto.getFlag()) : noCondition())
                .and(FORUM_TOPIC.STATUS.eq(0))
                .groupBy(FORUM_TOPIC.ID, USER.ID)
                .orderBy(sortColumn, false);
        return topicMapper.paginateAs(postDto.getPage(), postDto.getSize(), queryWrapper, UserTopicDto.class);
    }

    /**
     * 获取热门话题
     * @param pageDto 分页查询参数
     * @return 返回热门话题或空
     */
    @Override
    public Page<UserTopicDto> pageHotTopic(PageQueryDto pageDto) {
        QueryWrapper queryWrapper = initUserTopicQueryWrapper(1)
                .and(FORUM_TOPIC.IS_HOT.eq(true))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .groupBy(FORUM_TOPIC.ID, USER.ID)
                .orderBy(FORUM_TOPIC.STAR_COUNT.desc())
                .orderBy("comment_count", false)
                .orderBy(FORUM_TOPIC.CREATE_TIME.desc());
        return topicMapper.paginateAs(pageDto.getPage(), pageDto.getSize(), queryWrapper, UserTopicDto.class);
    }

    /**
     * 获取用户推荐话题
     * @param pageDto 分页查询参数
     * @param userId 用户id
     * @return 返回用户推荐话题列表或空
     */
    @Override
    public Page<UserTopicDto> pageProposeTopic(PageQueryDto pageDto, Long userId) {
        Page<Integer> topicIdPage = QueryChain.of(ProposeTopic.class)
                .select(PROPOSE_TOPIC.TOPIC_ID)
                .where(PROPOSE_TOPIC.USER_ID.eq(userId))
                .orderBy(PROPOSE_TOPIC.SIMILARITY.desc())
                .pageAs(new Page<>(pageDto.getPage(), pageDto.getSize()), Integer.class);
        Page<UserTopicDto> proposeTopicPage = new Page<>(null, topicIdPage.getPageNumber(),
                topicIdPage.getPageSize(), topicIdPage.getTotalRow());
        proposeTopicPage.setTotalPage(topicIdPage.getTotalPage());
        if (topicIdPage.getRecords() == null || topicIdPage.getRecords().isEmpty()) {
            return proposeTopicPage;
        }
        QueryWrapper queryWrapper = initUserTopicQueryWrapper(1)
                .and(FORUM_TOPIC.ID.in(topicIdPage.getRecords()))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .groupBy(FORUM_TOPIC.ID, USER.ID);
        List<UserTopicDto> proposeTopicList = topicMapper.selectListByQueryAs(queryWrapper, UserTopicDto.class);
        if (proposeTopicList != null && !proposeTopicList.isEmpty()) {
            proposeTopicPage.setRecords(proposeTopicList);
        }
        return proposeTopicPage;
    }

    /**
     * 通过标签Id获取话题列表
     * @param topicDto 查询参数
     * @return 返回话题列表或空
     */
    @Override
    public Page<UserTopicDto> pageTopicByLabelId(QueryTopicDto topicDto) {
        QueryWrapper queryWrapper = initUserTopicQueryWrapper(1);
        if (topicDto.getLabelId() != null) {
            queryWrapper.and("label_ids @> ARRAY [" + topicDto.getLabelId() + "]");
        }
        queryWrapper.and(FORUM_TOPIC.STATUS.eq(0));
        queryWrapper.orderBy(FORUM_TOPIC.CREATE_TIME.desc()).groupBy(FORUM_TOPIC.ID, USER.ID);
        return topicMapper.paginateAs(topicDto.getPage(), topicDto.getSize(), queryWrapper, UserTopicDto.class);
    }

    /**
     * 获取帖子/话题详情，用户可以获取到自己待审核以及审核未通过的帖子 其他用户无法获取
     * @param topicId 帖子/话题Id
     * @param userId 用户id
     * @return 返回详情或空
     */
    @Override
    public UserTopicDto queryTopic(Integer topicId, Long userId) {
        UserTopicDto topicDto = topicMapper.queryTopic(topicId, userId);
        ExceptionUtils.isConditionThrow(topicDto == null, HttpStatus.NOT_FOUND, "资源不存在");
        if (!topicDto.getUserId().equals(userId) && topicDto.getStatus() != 0) {
            ExceptionUtils.throwRequestException("帖子审核中或者审核未通过");
        }
        asyncTask.appendTopicViewCount(topicId, 1);
        return topicDto;
    }

    /**
     * 先判断待删除的帖子/话题是否由当前用户发布，如果不是则无权删除
     * 如果是，那么在帖子/话题删除后 需要同步删除用户收藏、用户推荐、评论信息
     * @param topicId 帖子/话题Id
     * @param userId 用户id
     */
    @Override
    @Transactional
    public void deleteTopic(Integer topicId, Long userId) {
        ForumTopic findTopic = topicMapper.selectOneByQuery(new QueryWrapper()
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.USER_ID, FORUM_TOPIC.STATUS)
                .where(FORUM_TOPIC.ID.eq(topicId)).and(FORUM_TOPIC.STATUS.ne(3)));
        ExceptionUtils.isConditionThrowRequest(findTopic == null, "需要删除的内容不存在");
        ExceptionUtils.isConditionThrowRequest(!findTopic.getUserId().equals(userId), "该用户无删除权限");
        int deleteResult = topicMapper.deleteTopic(topicId);
        if (deleteResult <= 0) {
            logger.info("删除帖子/话题失败，帖子/话题不存在");
            return;
        }
        if (findTopic.getStatus() == 0) {
            int result1 = extrasMapper.deleteTopicAfterUpdateUserStars(topicId);
            logger.info("删除帖子后移除用户收藏，TopicId:{}，受影响的用户数：{}", topicId, result1);
            int result2 = proposeMapper.deleteProposeTopicByTopicId(topicId);
            logger.info("删除帖子后移除用户推荐，TopicId:{}，受影响的用户数：{}", topicId, result2);
            int result3 = commentService.deleteCommentListByTopicId(topicId);
            logger.info("删除帖子后删除帖子评论，TopicId：{}，删除的评论数：{}", topicId, result3);
        }else {
            logger.info("帖子状态异常，跳过其他删除逻辑。帖子状态：{}", findTopic.getStatus());
        }
        logger.info("删除Topic成功，topicId：{}", topicId);
    }

    @Override
    public void starTopic(Integer topicId, Long userId) {
        int findCount = extrasMapper.countUserStarByTopicId(topicId, userId);
        ExceptionUtils.isConditionThrowRequest(findCount > 0, "当前帖子已经被收藏了");
        extrasMapper.saveTopicStar(topicId, userId);
        asyncTask.updateTopicStarCount(topicId, 1, true);
    }

    @Override
    public void unStartTopic(Integer topicId, Long userId) {
        int findCount = extrasMapper.countUserStarByTopicId(topicId, userId);
        ExceptionUtils.isConditionThrowRequest(findCount <= 0, "当前帖子还没有被收藏");
        extrasMapper.deleteTopicStar(topicId, userId);
        asyncTask.updateTopicStarCount(topicId, 1, false);
    }

    /**
     * 获取用户发布的所有帖子/话题
     * @param userTopicDto 查询参数
     * @param userId 用户Id
     * @return 返回帖子/话题列表或空
     */
    @Override
    public Page<UserTopicDto> pageUserPublishTopic(UserTopicQueryDto userTopicDto, Long userId) {
        QueryWrapper queryWrapper = this.initUserTopicQueryWrapper(userTopicDto.getType())
                .and(FORUM_TOPIC.USER_ID.eq(userId))
                .and(FORUM_TOPIC.STATUS.ne(3))
                .orderBy(FORUM_TOPIC.CREATE_TIME.desc())
                .groupBy(FORUM_TOPIC.ID, USER.ID);
        return topicMapper.paginateAs(userTopicDto.getPage(), userTopicDto.getSize(), queryWrapper, UserTopicDto.class);
    }


    @Override
    public Page<UserTopicDto> pageUserStarTopic(UserTopicQueryDto userTopicDto, Long userId) {
        UserExtras userExtras = QueryChain.of(UserExtras.class)
                .where(USER_EXTRAS.USER_ID.eq(userId))
                .one();
        Integer[] topicStars = userExtras.getTopicStars();
        if (topicStars == null || topicStars.length == 0) {
            return null;
        }
        QueryWrapper queryWrapper = this.initUserTopicQueryWrapper(userTopicDto.getType())
                .and(FORUM_TOPIC.ID.in((Object[]) topicStars))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .groupBy(FORUM_TOPIC.ID, USER.ID);
        return topicMapper.paginateAs(userTopicDto.getPage(), userTopicDto.getSize(), queryWrapper, UserTopicDto.class);

    }

    /**
     * 检查发帖内容是否存在敏感词
     * @param text 文本内容
     */
    private void checkSensitiveWord(String text) {
        String word = textFilter.filterText(text);
        ExceptionUtils.isConditionThrowRequest(word != null, "输入内容不允许包含'" + word + "'");
    }
    /**
     * 私有方法，检查用户是否触发了发帖限制
     * 触发逻辑为：一个小时内发布帖子/话题超过2条则出发  帖子与话题分开计算
     * @param userId 用户id
     * @param isPost true:帖子 false：话题
     */
    private void checkTopicPublishTime(Long userId, boolean isPost) {
        List<Object> createTimeList = QueryChain.of(ForumTopic.class)
                .select(FORUM_TOPIC.CREATE_TIME)
                .where(FORUM_TOPIC.USER_ID.eq(userId))
                .and(FORUM_TOPIC.TYPE.eq(isPost ? 0 : 1))
                .and(FORUM_TOPIC.STATUS.ne(3))
                .orderBy(FORUM_TOPIC.CREATE_TIME.desc())
                .limit(2)
                .listAs(Object.class);
        if (createTimeList == null || createTimeList.size() < 2) {
            return;
        }
        Timestamp lastCreateTime = (Timestamp) createTimeList.get(1);
        long lastMilli = lastCreateTime.getTime();
        long currentMillis = System.currentTimeMillis();
        if (currentMillis - lastMilli <= Duration.ofHours(PUBLISH_TOPIC_LIMIT_DATE_HOUR).toMillis()) {
            ExceptionUtils.throwRequestException(isPost ? "帖子发布过于频繁" : "话题发布过于频繁");
        }
    }

    private QueryWrapper initUserTopicQueryWrapper(Integer type) {
        return QueryWrapper.create()
                .select(FORUM_TOPIC.ALL_COLUMNS)
                .select(count(COMMENT.ID).as("comment_count"))
                .select(USER.NICK_NAME, USER.AVATAR)
                .from(FORUM_TOPIC)
                .leftJoin(COMMENT).on(COMMENT.TOPIC_ID.eq(FORUM_TOPIC.ID))
                .leftJoin(USER).on(USER.ID.eq(FORUM_TOPIC.USER_ID))
                .where(type != null ? FORUM_TOPIC.TYPE.eq(type) : noCondition());
    }
}
