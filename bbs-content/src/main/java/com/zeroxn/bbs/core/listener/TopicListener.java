package com.zeroxn.bbs.core.listener;

import com.zeroxn.bbs.base.constant.QueueConstant;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.core.solr.SolrRecordOperate;
import com.zeroxn.bbs.core.solr.TopicIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 21:34:31
 * @Description: 帖子/话题成功发布监听处理
 */
@Component
public class TopicListener {
    private static final Logger logger = LoggerFactory.getLogger(TopicListener.class);
    private final SolrRecordOperate recordOperate;
    public TopicListener(SolrRecordOperate recordOperate) {
        this.recordOperate = recordOperate;
    }

    /**
     * 处理帖子/话题生成关键字完毕、审核通过 成功发布的消息，保存内容到Solr的索引库中
     * @param topic 发布成功的帖子/话题
     */
    @RabbitListener(queues = QueueConstant.INDEX_TOPIC_QUEUE)
    public void listenerTopicPublishQueue(final ForumTopic topic) {
        logger.info("监听到帖子/话题发布成功消息，添加到Solr索引库，topicId:{}", topic.getId());
        final TopicIndex index = TopicIndex.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .content(topic.getContent())
                .contentKey(topic.getContentKey())
                .userId(topic.getUserId())
                .flag(topic.getFlag())
                .type(topic.getType())
                .build();
        recordOperate.saveRecord(index);
    }

    /**
     * 处理帖子/话题被删除的消息，删除Solr索引
     * @param topicId 被删除的帖子/话题Id
     */
    @RabbitListener(queues = QueueConstant.INDEX_DELETE_QUEUE)
    public void listenerTopicDeleteQueue(final Integer topicId) {
        logger.info("监听到帖子/话题删除消息，删除Solr索引，topicId:{}", topicId);
        final TopicIndex index = TopicIndex.builder().id(topicId).build();
        recordOperate.deleteRecord(index);
    }
}
