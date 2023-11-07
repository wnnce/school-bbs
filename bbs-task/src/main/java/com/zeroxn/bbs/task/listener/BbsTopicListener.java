package com.zeroxn.bbs.task.listener;

import com.zeroxn.bbs.base.constant.QueueConstant;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.base.entity.UserAction;
import com.zeroxn.bbs.task.service.ReviewTaskService;
import com.zeroxn.bbs.task.service.TopicService;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 21:53:42
 * @Description: 帖子/话题发布后，消息队列监听器
 */
@Component
public class BbsTopicListener {
    private static final Logger logger = LoggerFactory.getLogger(BbsTopicListener.class);
    private static final ExecutorService executors = Executors.newFixedThreadPool(2);
    private final ReviewTaskService taskService;
    private final TopicService topicService;
    private final TopicReviewService reviewService;
    public BbsTopicListener(TopicService topicService, ReviewTaskService taskService, TopicReviewService reviewService) {
        this.topicService = topicService;
        this.taskService = taskService;
        this.reviewService = reviewService;
    }

    /**
     * 当帖子/话题被删除时，同步删除其在redis中的关键字id缓存
     * @param topicId 被删除的话题ID
     */
    @RabbitListener(queues = QueueConstant.DELETE_QUEUE)
    public void listenerTopicDeleteQueue(Integer topicId) {
        logger.info("接受到话题删除消息，topicId：{}", topicId);
        topicService.batchUpdateRedisIdListByTopicIdList(Collections.singletonList(topicId));
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

    /**
     * 监听消息队列，接受到帖子消息后，启动两条异步线程分别执行帖子关键字生成和帖子审核的逻辑
     * 帖子关键字生成调用讯飞接口，生成完完毕后更新写入到数据库
     * 帖子审核使用百度接口，审核前先往审核任务表中写入当前帖子/话题的信息再开始审核，审核使用多线程对文本、图片、视频进行并行审核。
     * 当其中某一个接口调用失败返回null时，程序判断请求失败 更新审核任务表中各阶段字段的值，并不对帖子的状态进行修改 由定时任务再次重新审核
     * 如果某一项内容返回为false,那么证明帖子内包含有敏感内容，更新帖子状态为审核未通过，同时删除审核任务表的数据
     * 如果接口全部返回true,证明帖子可以正常发布，更新帖子状态为正常，删除审核任务表中的数据
     * @param topic 发布的帖子/话题对象
     */
    @RabbitListener(queues = QueueConstant.TOPIC_QUEUE)
    public void listenerBbsTopicQueue(ForumTopic topic) {
        CompletableFuture<Void> makeContentKeyFuture = CompletableFuture.runAsync(() -> {
            logger.info("接收到消息，开始生成关键字。topicId:{}，创建时间：{}", topic.getId(), topic.getCreateTime());
            topicService.handlerTopicKeyword(topic);
        }, executors);
        CompletableFuture<Void> reviewFuture = CompletableFuture.runAsync(() -> {
            taskService.addReviewTask(topic.getId());
            CompletableFuture<Optional<Boolean>> textResultFuture = reviewService.asyncReviewTopicText(topic.getTitle() + "," + topic.getContent());
            CompletableFuture<Optional<Boolean>> videoResultFuture = reviewService.asyncReviewTopicVideo(topic.getVideoUrl());
            CompletableFuture<Optional<Boolean>> imageResultFuture = reviewService.asyncReviewTopicImage(topic.getImageUrls());
            CompletableFuture.allOf(textResultFuture, videoResultFuture, imageResultFuture).join();
            try {
                Boolean textResult = null, videoResult = null, imageResult = null;
                if (textResultFuture.get().isPresent()) {
                    textResult = textResultFuture.get().get();
                }
                if (videoResultFuture.get().isPresent()) {
                    videoResult = textResultFuture.get().get();
                }
                if (imageResultFuture.get().isPresent()) {
                    imageResult = imageResultFuture.get().get();
                }
                logger.info("帖子审核结果，topicId:{}, textResult:{}, videoResult:{}, imageResult:{}", topic.getId(), textResult, videoResult, imageResult);
                if (textResult == null || videoResult == null || imageResult == null) {
                    logger.info("帖子审核有调用失败结果，写入异常任务数据库");
                    taskService.updateReviewTaskStage(topic.getId(), textResult, imageResult, videoResult);
                    return;
                } else if (!textResult || !videoResult || !imageResult) {
                    logger.info("帖子审核结果有敏感内容，审核不通过");
                    topicService.updateTopicStatus(topic.getId(), 2);
                } else {
                    logger.info("帖子审核通过，允许发布");
                    topicService.updateTopicStatus(topic.getId(), 0);
                    if (topic.getType() == 1) {
                        // 添加到redis的关键字话题Id列表缓存
                        topicService.addTopicToRedisIdList(topic.getId());
                    }
                }
                logger.info("帖子审核没有调用异常，删除异常任务数据");
                taskService.deleteReviewTaskByTopicId(topic.getId());
            } catch (Exception e) {
                logger.error("执行帖子审查失败，错误信息：{}", e.getMessage());
            }
        });
        CompletableFuture.allOf(makeContentKeyFuture, reviewFuture).join();
    }
}
