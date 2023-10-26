package com.zeroxn.bbs.task.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.zeroxn.bbs.base.entity.ReviewTask;
import com.zeroxn.bbs.task.mapper.ReviewTaskMapper;
import com.zeroxn.bbs.task.service.ReviewTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.zeroxn.bbs.base.entity.table.ReviewTaskTableDef.REVIEW_TASK;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 12:43:56
 * @Description: 帖子审核记录服务层实现类
 */
@Service
public class ReviewTaskServiceImpl implements ReviewTaskService {
    private final ReviewTaskMapper taskMapper;
    public ReviewTaskServiceImpl(ReviewTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    /**
     * 根据topicId添加审核记录
     * @param topicId 帖子Id
     */
    @Override
    public void addReviewTask(Integer topicId) {
        ReviewTask reviewTask = ReviewTask.builder().topicId(topicId).build();
        taskMapper.insertSelective(reviewTask);
    }

    /**
     * 更新审核任务记录
     * @param topicId 帖子Id
     * @param stage1 一阶段任务状态
     * @param stage2 二阶段任务状态
     * @param stage3 三阶段任务状态
     */
    @Override
    public void updateReviewTaskStage(Integer topicId, Boolean stage1, Boolean stage2, Boolean stage3) {
        ReviewTask reviewTask = UpdateEntity.of(ReviewTask.class);
        reviewTask.setStage1(stage1);
        reviewTask.setStage2(stage2);
        reviewTask.setStage3(stage3);
        taskMapper.updateByQuery(reviewTask, new QueryWrapper().where(REVIEW_TASK.TOPIC_ID.eq(topicId)));
    }

    /**
     * 通过实体类更新审核任务记录
     * @param task 实体类对象
     */
    @Override
    public void updateReviewTask(ReviewTask task) {
        taskMapper.update(task);
    }

    /**
     * 通过topicId删除审核任务记录
     * @param topicId 帖子Id
     */
    @Override
    public void deleteReviewTaskByTopicId(Integer topicId) {
        taskMapper.deleteByQuery(new QueryWrapper().where(REVIEW_TASK.TOPIC_ID.eq(topicId)));
    }

    /**
     * 通过Id删除单条审核任务记录
     * @param taskId 任务Id
     */
    @Override
    public void deleteReviewTask(Integer taskId) {
        taskMapper.deleteById(taskId);
    }

    /**
     * 获取所有重试次数小于4的审核任务记录
     * @return 审核记录列表或空
     */
    @Override
    public List<ReviewTask> listAllReviewTask() {
        return taskMapper.selectListByQuery(new QueryWrapper().where(REVIEW_TASK.RETRY_COUNT.le(4)));
    }
}
