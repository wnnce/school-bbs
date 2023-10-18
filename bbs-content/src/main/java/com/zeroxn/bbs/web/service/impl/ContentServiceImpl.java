package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.base.entity.ProposeTopic;
import com.zeroxn.bbs.base.entity.UserExtras;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.QueryPostDto;
import com.zeroxn.bbs.web.dto.UserTopicDto;
import com.zeroxn.bbs.web.dto.UserTopicQueryDto;
import com.zeroxn.bbs.web.mapper.ForumTopicMapper;
import com.zeroxn.bbs.web.mapper.ProposeTopicMapper;
import com.zeroxn.bbs.web.mapper.UserExtrasMapper;
import com.zeroxn.bbs.web.service.ContentService;
import com.zeroxn.bbs.web.service.async.GlobalAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.*;
import static com.zeroxn.bbs.core.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.core.entity.table.ForumTopicTableDef.FORUM_TOPIC;
import static com.zeroxn.bbs.core.entity.table.ProposeTopicTableDef.PROPOSE_TOPIC;
import static com.zeroxn.bbs.core.entity.table.UserExtrasTableDef.USER_EXTRAS;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 11:35:14
 * @Description: 帖子/话题服务层实现类
 */
@Service
public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private static final int PUBLISH_TOPIC_LIMIT_DATE_HOUR = 1;
    private final ForumTopicMapper topicMapper;
    private final UserExtrasMapper extrasMapper;
    private final ProposeTopicMapper proposeMapper;
    private final GlobalAsyncTask asyncTask;

    public ContentServiceImpl(ForumTopicMapper topicMapper, GlobalAsyncTask asyncTask, UserExtrasMapper extrasMapper,
                              ProposeTopicMapper proposeMapper) {
        this.topicMapper = topicMapper;
        this.asyncTask = asyncTask;
        this.extrasMapper = extrasMapper;
        this.proposeMapper = proposeMapper;
    }

    @Override
    public void saveTopic(ForumTopic topic) {
        ExceptionUtils.isConditionThrowRequest(checkTopicPublishTime(topic.getUserId(), false), "话题发布过于频繁");
        topic.setType(1);
        topicMapper.insertSelective(topic);
        logger.info("话题保存成功，TopicId:{}", topic.getId());
        // 调用异步方法生成话题关键字
        asyncTask.handlerTopicContentKey(topic);
        // 调用异步方法处理话题推送
        asyncTask.handlerTopicPropose(topic);
    }

    @Override
    public void savePost(ForumTopic post) {
        ExceptionUtils.isConditionThrowRequest(checkTopicPublishTime(post.getUserId(), true), "帖子发布过于频繁");
        post.setType(0);
        topicMapper.insertSelective(post);
        logger.info("帖子保存成功，TopicId：{}", post.getId());
        // 调用异步方法生成帖子关键字
        asyncTask.handlerTopicContentKey(post);
        logger.info("return");
    }

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
        QueryWrapper queryWrapper = initUserTopicQueryWrapper()
                .where(postDto.getFlag() != null ? FORUM_TOPIC.FLAG.eq(postDto.getFlag()) : noCondition())
                .groupBy(FORUM_TOPIC.ID)
                .orderBy(sortColumn, false);
        return topicMapper.paginateAs(postDto.getPage(), postDto.getSize(), queryWrapper, UserTopicDto.class);
    }

    @Override
    public Page<UserTopicDto> pageHotTopic(PageQueryDto pageDto) {
        QueryWrapper queryWrapper = initUserTopicQueryWrapper()
                .where(FORUM_TOPIC.IS_HOT.eq(true))
                .groupBy(FORUM_TOPIC.ID)
                .orderBy(FORUM_TOPIC.STAR_COUNT.desc())
                .orderBy("comment_count", false)
                .orderBy(FORUM_TOPIC.CREATE_TIME.desc());
        return topicMapper.paginateAs(pageDto.getPage(), pageDto.getSize(), queryWrapper, UserTopicDto.class);
    }

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
        QueryWrapper queryWrapper = initUserTopicQueryWrapper()
                .where(FORUM_TOPIC.ID.in(topicIdPage.getRecords()))
                .groupBy(FORUM_TOPIC.ID);
        List<UserTopicDto> proposeTopicList = topicMapper.selectListByQueryAs(queryWrapper, UserTopicDto.class);
        if (proposeTopicList != null && !proposeTopicList.isEmpty()) {
            proposeTopicPage.setRecords(proposeTopicList);
        }
        return proposeTopicPage;
    }

    @Override
    public UserTopicDto queryTopic(Integer topicId, Long userId) {
        UserTopicDto topicDto = topicMapper.queryTopic(topicId, userId);
        ExceptionUtils.isConditionThrow(topicDto == null, HttpStatus.NOT_FOUND, "资源不存在");
        asyncTask.appendTopicViewCount(topicId, 1);
        return topicDto;
    }

    @Override
    @Transactional
    public void deleteTopic(Integer topicId, Long userId) {
        ForumTopic findTopic = topicMapper.selectOneByQuery(new QueryWrapper()
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.USER_ID)
                .where(FORUM_TOPIC.ID.eq(topicId)));
        ExceptionUtils.isConditionThrowRequest(findTopic == null, "需要删除的内容不存在");
        ExceptionUtils.isConditionThrowRequest(findTopic.getUserId() != userId, "该用户无删除权限");
        int deleteResult = topicMapper.deleteById(topicId);
        if (deleteResult > 0) {
            int result1 = extrasMapper.deleteTopicAfterUpdateUserStars(topicId);
            logger.info("删除帖子后移除用户收藏，TopicId:{}，受影响的用户数：{}", topicId, result1);
            int result2 = proposeMapper.deleteProposeTopicByTopicId(topicId);
            logger.info("删除帖子后移除用户推荐，TopicId:{}，受影响的用户数：{}", topicId, result2);
            logger.info("删除Topic成功，topicId：{}", topicId);
        }else {
            logger.info("删除帖子/话题失败，帖子/话题不存在");
        }
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

    @Override
    public Page<UserTopicDto> pageUserPublishTopic(UserTopicQueryDto userTopicDto, Long userId) {
        QueryWrapper queryWrapper = this.initUserTopicQueryWrapper()
                .where(FORUM_TOPIC.USER_ID.eq(userId))
                .and(userTopicDto.getType() != null ? FORUM_TOPIC.TYPE.eq(userTopicDto.getType()) : noCondition())
                .orderBy(FORUM_TOPIC.CREATE_TIME.desc())
                .groupBy(FORUM_TOPIC.ID);
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
        QueryWrapper queryWrapper = this.initUserTopicQueryWrapper()
                .where(FORUM_TOPIC.ID.in(Arrays.stream(topicStars).toArray()))
                .and(userTopicDto.getType() != null ? FORUM_TOPIC.TYPE.eq(userTopicDto.getType()) : noCondition())
                .groupBy(FORUM_TOPIC.ID);
        return topicMapper.paginateAs(userTopicDto.getPage(), userTopicDto.getSize(), queryWrapper, UserTopicDto.class);
    }

    private boolean checkTopicPublishTime(Long userId, boolean isPost) {
        List<Object> createTimeList = QueryChain.of(ForumTopic.class)
                .select(FORUM_TOPIC.CREATE_TIME)
                .where(FORUM_TOPIC.USER_ID.eq(userId))
                .and(FORUM_TOPIC.TYPE.eq(isPost ? 0 : 1))
                .orderBy(FORUM_TOPIC.CREATE_TIME.desc())
                .limit(2)
                .listAs(Object.class);
        if (createTimeList == null || createTimeList.size() < 2) {
            return false;
        }
        Timestamp lastCreateTime = (Timestamp) createTimeList.get(1);
        long lastMilli = lastCreateTime.getTime();
        long currentMillis = System.currentTimeMillis();
        return currentMillis - lastMilli <= Duration.ofHours(PUBLISH_TOPIC_LIMIT_DATE_HOUR).toMillis();
    }

    private QueryWrapper initUserTopicQueryWrapper() {
        return QueryWrapper.create()
                .select(FORUM_TOPIC.ALL_COLUMNS)
                .select(count(COMMENT.ID).as("comment_count"))
                .from(FORUM_TOPIC)
                .leftJoin(COMMENT).on(COMMENT.TOPIC_ID.eq(FORUM_TOPIC.ID));
    }
}
