package com.zeroxn.bbs.task.service;

import com.zeroxn.bbs.base.entity.ReviewTask;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 12:41:32
 * @Description: 审核任务记录服务层
 */
public interface ReviewTaskService {
    void addReviewTask(Integer topicId);
    void updateReviewTaskStage(Integer topicId, Boolean stage1, Boolean stage2, Boolean stage3);
    void updateReviewTask(ReviewTask task);
    void deleteReviewTaskByTopicId(Integer topicId);
    void deleteReviewTask(Integer taskId);
    List<ReviewTask> listAllReviewTask();
}
