package com.zeroxn.bbs.task.listener;

import com.zeroxn.bbs.base.constant.QueueConstant;
import com.zeroxn.bbs.task.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @Author: lisang
 * @DateTime: 2023-10-28 21:24:45
 * @Description: 处理删除话题后的消息
 */
@Component
public class BbsDeleteTopicListener {
    private static final Logger logger = LoggerFactory.getLogger(BbsDeleteTopicListener.class);
    private final TopicService topicService;
    public BbsDeleteTopicListener(TopicService topicService) {
        this.topicService = topicService;
    }

    @RabbitListener(queues = QueueConstant.DELETE_QUEUE)
    public void listenerTopicDeleteQueue(Integer topicId) {
        logger.info("接受到话题删除消息，topicId：{}", topicId);
        topicService.batchUpdateRedisIdListByTopicIdList(Collections.singletonList(topicId));
    }
}

