package com.zeroxn.bbs.task.handler.review;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 17:37:25
 * @Description: 帖子审核责任器链中的文本审核器
 */
public class TextReviewHandler extends ReviewHandler{
    public TextReviewHandler(TopicReviewService reviewService) {
        super(reviewService);
    }

    /**
     * 提供帖子文本审核，如果stage不匹配则调用下一个执行器
     * @param result 上一次的请求结果
     * @param stage 审核什么内容 1：文本 2：图像 3：视频
     * @param topic 需要审核的帖子/话题对象
     * @param logger 在线Logger
     * @return result不为null直接返回result 否则返回责任链的执行结果
     */
    @Override
    public Boolean execute(Boolean result, int stage, ForumTopic topic, OmsLogger logger) {
        if (result != null) {
            return result;
        }
        if (stage == 1) {
            CompletableFuture<Optional<Boolean>> resultFuture = reviewService.asyncReviewTopicText(topic.getTitle() + "," + topic.getContent());
            try {
                Optional<Boolean> optionalResult = resultFuture.get();
                if (optionalResult.isPresent()) {
                    return optionalResult.get();
                }
            }catch (Exception e) {
                logger.error("帖子文本审查异常，错误消息：{}", e.getMessage());
            }
        }else if (nextHandler != null) {
            return nextHandler.execute(result, stage, topic, logger);
        }
        return null;
    }
}
