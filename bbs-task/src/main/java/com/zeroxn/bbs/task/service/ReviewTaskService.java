package com.zeroxn.bbs.task.service;

import scala.Int;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 12:41:32
 * @Description:
 */
public interface ReviewTaskService {
    void addReviewTask(Integer topicId);
    void updateReviewTaskStage(Integer topicId, Boolean stage1, Boolean stage2, Boolean stage3);
    void deleteReviewTask(Integer topicId);
}
