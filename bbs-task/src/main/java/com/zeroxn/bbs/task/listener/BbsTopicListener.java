package com.zeroxn.bbs.task.listener;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 21:53:42
 * @Description: 帖子/话题发布后，消息队列监听器
 */
@Component
public class BbsTopicListener {
    private static final Logger logger = LoggerFactory.getLogger(BbsTopicListener.class);

    private final TopicService topicService;

    public BbsTopicListener(TopicService topicService) {
        this.topicService = topicService;
    }
    @RabbitListener(queues = "bbs.topic")
    public void listenerBbsTopicQueue(ForumTopic topic) {
        logger.info("接收到消息，开始生成关键字。topicId:{}，创建时间：{}", topic.getId(), topic.getCreateTime());
        topicService.handlerTopicKeyword(topic);
    }
}
