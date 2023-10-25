package com.zeroxn.bbs.task.listener;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.filter.ContentSecurityReview;
import com.zeroxn.bbs.task.service.ReviewTaskService;
import com.zeroxn.bbs.task.service.TopicService;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    @RabbitListener(queues = "bbs.topic")
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
            try {
                CompletableFuture.allOf(textResultFuture, videoResultFuture, imageResultFuture).get();
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
                }
                logger.info("帖子审核没有调用异常，删除异常任务数据");
                taskService.deleteReviewTask(topic.getId());
            } catch (Exception e) {
                logger.error("");
            }
        });
        try {
            CompletableFuture.allOf(makeContentKeyFuture, reviewFuture).get();
        }catch (Exception e) {
            logger.info("支持生成关键字或帖子审核任务异常，错误信息：{}", e.getMessage());
        }
    }
}
