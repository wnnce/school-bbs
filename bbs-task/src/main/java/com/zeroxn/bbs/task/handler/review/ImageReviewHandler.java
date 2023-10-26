package com.zeroxn.bbs.task.handler.review;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 17:42:38
 * @Description: 责任链中的图像审核器
 */
public class ImageReviewHandler extends ReviewHandler{
    public ImageReviewHandler(TopicReviewService reviewService) {
        super(reviewService);
    }

    /**
     * 提供图像内容的审核
     * @param result 上一次的请求结果
     * @param stage 审核什么内容 1：文本 2：图像 3：视频
     * @param topic 需要审核的帖子/话题对象
     * @param logger 在线Logger
     */
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
