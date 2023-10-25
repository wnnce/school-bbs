package com.zeroxn.bbs.task.service.impl;

import com.hankcs.hanlp.HanLP;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.UpdateEntity;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.analytics.TextAnalytics;
import com.zeroxn.bbs.task.dao.TopicDao;
import com.zeroxn.bbs.task.mapper.CommentMapper;
import com.zeroxn.bbs.task.mapper.ForumTopicMapper;
import com.zeroxn.bbs.task.mapper.ProposeTopicMapper;
import com.zeroxn.bbs.task.mapper.UserExtrasMapper;
import com.zeroxn.bbs.task.service.TopicService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;
import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.count;
import static com.mybatisflex.core.query.QueryMethods.max;
import static com.zeroxn.bbs.base.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.base.entity.table.ForumTopicTableDef.FORUM_TOPIC;
import static com.zeroxn.bbs.base.entity.table.ProposeTopicTableDef.PROPOSE_TOPIC;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 18:46:38
 * @Description:
 */
@Service
public class TopicServiceImpl implements TopicService {
    private static final Logger logger = LoggerFactory.getLogger(TopicServiceImpl.class);
    private final TextAnalytics textAnalytics;
    private final ForumTopicMapper topicMapper;
    private final CommentMapper commentMapper;
    private final UserExtrasMapper extrasMapper;
    private final ProposeTopicMapper proposeMapper;
    @Value("${analysis.size}")
    private Integer keywordSize = 5;

    public TopicServiceImpl(TextAnalytics textAnalytics, ForumTopicMapper topicMapper, CommentMapper commentMapper,
                            UserExtrasMapper extrasMapper, ProposeTopicMapper proposeMapper) {
        this.textAnalytics = textAnalytics;
        this.topicMapper = topicMapper;
        this.commentMapper = commentMapper;
        this.extrasMapper = extrasMapper;
        this.proposeMapper = proposeMapper;
    }
    @Override
    public void handlerTopicKeyword(ForumTopic topic) {
        String document = topic.getTitle() + topic.getContent();
        List<String> keywordList = null;
        if (document.length() > 30000) {
            logger.warn("文本长度超过讯飞限制，本地生成关键字。");
            keywordList = HanLP.extractKeyword(document, keywordSize);
        }else {
            keywordList = textAnalytics.keywordsExtraction(document, keywordSize);
            if (keywordList == null) {
                logger.error("讯飞生成文章关键子失败，调用本地生成");
            }
            keywordList = HanLP.extractKeyword(document, keywordSize);
        }
        logger.info("关键字生成完成，数量：{}", keywordList.size());
        long count = QueryChain.of(ForumTopic.class)
                .where(FORUM_TOPIC.ID.eq(topic.getId()))
                .count();
        if (count <= 0) {
            logger.warn("帖子已被删除，放弃保存，topicId:{}", topic.getId());
        }else {
            String contentKey = Strings.join(keywordList, ',');
            boolean result = UpdateChain.of(ForumTopic.class)
                    .set(FORUM_TOPIC.CONTENT_KEY, contentKey)
                    .where(FORUM_TOPIC.ID.eq(topic.getId()))
                    .update();
            logger.info("更新帖子关键字完成，topicId：{}，影响行数：{}", topic.getId(), result);
        }
    }

    @Override
    public List<TopicDao> listAllTopic() {
        return QueryChain.of(ForumTopic.class)
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.CREATE_TIME, FORUM_TOPIC.IS_HOT, FORUM_TOPIC.VIEW_COUNT, FORUM_TOPIC.STAR_COUNT)
                .select(count(COMMENT.ID).as("comment_count"), max(COMMENT.CREATE_TIME).as("last_comment_time"))
                .from(FORUM_TOPIC)
                .leftJoin(COMMENT).on(COMMENT.TOPIC_ID.eq(FORUM_TOPIC.ID))
                .where(FORUM_TOPIC.TYPE.eq(1))
                .groupBy(FORUM_TOPIC.ID)
                .listAs(TopicDao.class);
    }

    @Override
    @Transactional
    public boolean updateTopicHot(List<Integer> hotTopicIdList) {
        UpdateChain.of(ForumTopic.class)
                .set(FORUM_TOPIC.IS_HOT, false)
                .where(FORUM_TOPIC.ID.notIn(hotTopicIdList))
                .and(FORUM_TOPIC.TYPE.eq(1))
                .update();
        return UpdateChain.of(ForumTopic.class)
                .set(FORUM_TOPIC.IS_HOT, true)
                .where(FORUM_TOPIC.ID.in(hotTopicIdList))
                .and(FORUM_TOPIC.TYPE.eq(1))
                .update();
    }

    @Override
    public List<TopicDao> listNotHotTopic() {
        return QueryChain.of(ForumTopic.class)
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.CREATE_TIME, FORUM_TOPIC.VIEW_COUNT, FORUM_TOPIC.STAR_COUNT)
                .select(count(COMMENT.ID).as("comment_count"), max(COMMENT.CREATE_TIME).as("last_comment_time"))
                .from(FORUM_TOPIC)
                .leftJoin(COMMENT).on(COMMENT.TOPIC_ID.eq(FORUM_TOPIC.ID))
                .where(FORUM_TOPIC.TYPE.eq(1))
                .and(FORUM_TOPIC.IS_HOT.eq(false))
                .and("create_time < current_timestamp - interval '1 day'")
                .groupBy(FORUM_TOPIC.ID)
                .listAs(TopicDao.class);
    }

    @Override
    @Transactional
    public boolean deleteTopicByTopicIdList(List<Integer> topicIdList, OmsLogger logger) {
        int result1 = topicMapper.deleteBatchByIds(topicIdList);
        logger.info("话题表删除完成，影响行数：{}", result1);
        int result2 = commentMapper.deleteByQuery(new QueryWrapper().where(COMMENT.TOPIC_ID.in(topicIdList)));
        logger.info("评论表删除完成，影响行数：{}", result2);
        String topicIds = String.join(",", topicIdList.stream().map(Object::toString).toArray(String[]::new));
        int result3 = extrasMapper.batchDeleteUserStarByTopicIdList(topicIds);
        logger.info("用户收藏表删除完成，影响行数：{}", result3);
        int result4 = proposeMapper.deleteByQuery(new QueryWrapper().where(PROPOSE_TOPIC.TOPIC_ID.in(topicIdList)));
        logger.info("用户推荐表删除完成，影响行数：{}", result4);
        logger.info("{}个话题删除成功", topicIdList.size());
        return true;
    }

    @Override
    public void updateTopicStatus(Integer topicId, int status) {
        int result = topicMapper.updateTopicStatus(topicId, status);
        logger.info("更新帖子状态完成，topicId：{}, newStatus:{}, 影响行数:{}", topicId, status, result);
    }
}
