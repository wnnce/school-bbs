package com.zeroxn.bbs.task.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.zeroxn.bbs.base.entity.ReviewTask;
import com.zeroxn.bbs.task.mapper.ReviewTaskMapper;
import com.zeroxn.bbs.task.service.ReviewTaskService;
import org.springframework.stereotype.Service;
import scala.Int;

import static com.zeroxn.bbs.base.entity.table.ReviewTaskTableDef.REVIEW_TASK;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 12:43:56
 * @Description:
 */
@Service
public class ReviewTaskServiceImpl implements ReviewTaskService {
    private final ReviewTaskMapper taskMapper;
    public ReviewTaskServiceImpl(ReviewTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }
    @Override
    public void addReviewTask(Integer topicId) {
        ReviewTask reviewTask = ReviewTask.builder().topicId(topicId).build();
        taskMapper.insertSelective(reviewTask);
    }

    @Override
    public void updateReviewTaskStage(Integer topicId, Boolean stage1, Boolean stage2, Boolean stage3) {
        ReviewTask reviewTask = UpdateEntity.of(ReviewTask.class);
        reviewTask.setStage1(stage1);
        reviewTask.setStage2(stage2);
        reviewTask.setStage3(stage3);
        taskMapper.updateByQuery(reviewTask, new QueryWrapper().where(REVIEW_TASK.TOPIC_ID.eq(topicId)));
    }

    @Override
    public void deleteReviewTask(Integer topicId) {
        taskMapper.deleteByQuery(new QueryWrapper().where(REVIEW_TASK.TOPIC_ID.eq(topicId)));
    }
}
