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

    /**
     * 具体的执行逻辑 由子类实现
     * @param result 上一次的请求结果
     * @param stage 审核什么内容 1：文本 2：图像 3：视频
     * @param topic 需要审核的帖子/话题对象
     * @param logger 在线Logger
     * @return 返回审核结果 调用失败返回null
     */
    public abstract Boolean execute(Boolean result, int stage, ForumTopic topic, OmsLogger logger);
}
