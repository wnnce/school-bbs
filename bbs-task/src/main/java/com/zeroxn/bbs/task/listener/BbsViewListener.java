package com.zeroxn.bbs.task.listener;

import com.zeroxn.bbs.base.constant.QueueConstant;
import com.zeroxn.bbs.base.entity.UserAction;
import com.zeroxn.bbs.task.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: lisang
 * @DateTime: 2023-10-28 19:15:49
 * @Description: RabbitMQ监听用户点击行为的处理器
 */
@Component
public class BbsViewListener {

    private static final Logger logger = LoggerFactory.getLogger(BbsViewListener.class);
    private final TopicService topicService;
    public BbsViewListener(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * 接受到用户点击帖子/话题后的行为时，先通过id获取帖子/话题详情，再通过该详情的关键字列表去redis缓存中查询对应的话题id列表
     * 取第一位（最后添加）, 然后将获取到的话题id写入到用户推荐表
     * @param userAction 用户点击行为对象
     */
    @RabbitListener(queues = QueueConstant.VIEW_QUEUE)
    public void listenerViewQueue(UserAction userAction) {
        logger.info("监听到用户点击行为，topicId：{}，开始处理", userAction.getTopicId());
        topicService.handlerViewTopicAfterPropose(userAction.getUserId(), userAction.getTopicId());
    }
}
