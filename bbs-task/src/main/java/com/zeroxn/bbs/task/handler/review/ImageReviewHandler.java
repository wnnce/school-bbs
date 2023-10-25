package com.zeroxn.bbs.task.handler.review;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 17:42:38
 * @Description:
 */
public class ImageReviewHandler extends ReviewHandler{
    public ImageReviewHandler(TopicReviewService reviewService) {
        super(reviewService);
    }

    @Override
    public Boolean execute(Boolean result, int stage, ForumTopic topic, OmsLogger logger) {
        if (stage == 2) {
            CompletableFuture<Optional<Boolean>> resultFuture = reviewService.asyncReviewTopicImage(topic.getImageUrls());
            try {
                Optional<Boolean> optionalResult = resultFuture.get();
                if (optionalResult.isPresent()) {
                    return optionalResult.get();
                }
            }catch (Exception e) {
                logger.error("帖子图片审查异常，错误信息：{}", e.getMessage());
            }
        }else if (nextHandler != null) {
            return nextHandler.execute(result, stage, topic, logger);
        }
        return null;
    }
}
