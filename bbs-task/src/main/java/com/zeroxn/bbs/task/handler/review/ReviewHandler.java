package com.zeroxn.bbs.task.handler.review;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import tech.powerjob.worker.log.OmsLogger;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 17:30:37
 * @Description: 帖子审核责任器链
 */
public abstract class ReviewHandler {
    protected final TopicReviewService reviewService;

    public ReviewHandler(TopicReviewService reviewService) {
        this.reviewService = reviewService;
    }
    protected ReviewHandler nextHandler;

    public void setNextHandler(ReviewHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    public abstract Boolean execute(Boolean result, int stage, ForumTopic topic, OmsLogger logger);
}
