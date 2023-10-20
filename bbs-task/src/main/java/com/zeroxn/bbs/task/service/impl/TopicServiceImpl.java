package com.zeroxn.bbs.task.service.impl;

import com.hankcs.hanlp.HanLP;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.analytics.TextAnalytics;
import com.zeroxn.bbs.task.dao.TopicDao;
import com.zeroxn.bbs.task.mapper.ForumTopicMapper;
import com.zeroxn.bbs.task.service.TopicService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.count;
import static com.mybatisflex.core.query.QueryMethods.max;
import static com.zeroxn.bbs.base.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.base.entity.table.ForumTopicTableDef.FORUM_TOPIC;

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

    @Value("${analysis.size}")
    private Integer keywordSize = 5;

    public TopicServiceImpl(TextAnalytics textAnalytics, ForumTopicMapper topicMapper) {
        this.textAnalytics = textAnalytics;
        this.topicMapper = topicMapper;
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
}
