package com.zeroxn.bbs.task.handler.review;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 17:45:17
 * @Description:
 */
public class VideoReviewHandler extends ReviewHandler{
    public VideoReviewHandler(TopicReviewService reviewService) {
        super(reviewService);
    }

    @Override
    public Boolean execute(Boolean result, int stage, ForumTopic topic, OmsLogger logger) {
        if (stage == 3) {
            CompletableFuture<Optional<Boolean>> resultFuture = reviewService.asyncReviewTopicVideo(topic.getVideoUrl());
            try {
                Optional<Boolean> optionalResult = resultFuture.get();
                if (optionalResult.isPresent()) {
                    return optionalResult.get();
                }
            }catch (Exception e) {
                logger.error("帖子视频审查异常，错误信息：{}", e.getMessage());
            }
        } else if (nextHandler != null) {
            return nextHandler.execute(result, stage, topic, logger);
        }
        return null;
    }
}
