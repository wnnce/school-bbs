package com.zeroxn.bbs.task.processors;

import com.zeroxn.bbs.base.constant.QueueConstant;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.base.entity.ReviewTask;
import com.zeroxn.bbs.task.handler.review.ReviewHandler;
import com.zeroxn.bbs.task.service.ReviewTaskService;
import com.zeroxn.bbs.task.service.TopicService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 16:55:36
 * @Description: 帖子审查异常任务处理
 */
@Component("topicReviewProcessor")
public class TopicReviewProcessor implements BasicProcessor {
    private final ReviewTaskService taskService;
    private final TopicService topicService;
    private final ReviewHandler reviewHandler;
    private final RabbitTemplate rabbitTemplate;
    public TopicReviewProcessor(ReviewTaskService taskService, ReviewHandler reviewHandler, TopicService topicService,
                                RabbitTemplate rabbitTemplate) {
        this.taskService = taskService;
        this.topicService = topicService;
        this.reviewHandler = reviewHandler;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 处理帖子审核异常的任务方法
     * 查询审核异常任务表所有重试次数小于等于3的记录，判断任务是否包含失败项，如果包含则更新帖子为审核不通过状态，删除当前记录
     * 如果不包含失败项，通过帖子Id查询该帖子是否已被删除，如果被删除也删除当前记录
     * 所有条件都通过时，调用责任链执行各阶段任务，执行完成后的处理逻辑和审核帖子一致
     * @param taskContext 任务平台上下文
     * @return 返回方法的运行结果
     * @throws Exception 异常
     */
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        OmsLogger logger = taskContext.getOmsLogger();
        List<ReviewTask> reviewTaskList = taskService.listAllReviewTask();
        if (reviewTaskList == null || reviewTaskList.isEmpty()) {
            logger.info("获取到的异常审查任务为空，任务结束");
            return new ProcessResult(true);
        }
        logger.info("获取异常审查任务成功，任务数量:{}", reviewTaskList.size());
        reviewTaskList.forEach(task -> {
            Boolean stage1 = task.getStage1();
            Boolean stage2 = task.getStage2();
            Boolean stage3 = task.getStage3();
            if ((stage1 != null && !stage1) || (stage2 != null && !stage2) || (stage3 != null && !stage3)) {
                logger.info("当前任务有审查失败字段，删除任务跳过执行");
                topicService.updateTopicStatus(task.getTopicId(), 2);
                return;
            }
            ForumTopic topic = topicService.queryReviewTopic(task.getTopicId());
            if (topic == null) {
                logger.info("帖子被删除，清除当前异常审查任务，taskId：{}, topicId: {}", task.getId(), task.getTopicId());
                taskService.deleteReviewTask(task.getId());
                return;
            }
            stage1 = reviewHandler.execute(stage1, 1, topic, logger);
            stage2 = reviewHandler.execute(stage2, 2, topic, logger);
            stage3 = reviewHandler.execute(stage3, 3, topic, logger);
            if (stage1 == null || stage2 == null || stage3 == null) {
                task.setStage1(stage1);
                task.setStage2(stage2);
                task.setStage3(stage3);
                task.setRetryCount(task.getRetryCount() + 1);
                taskService.updateReviewTask(task);
                logger.info("帖子审查异常任务重试异常，taskId：{},重试次数：{}", task.getId(), task.getRetryCount());
                return;
            } else if (!stage1 || !stage2 || !stage3) {
                logger.info("帖子审查不通过，删除任务，更改帖子状态");
                topicService.updateTopicStatus(task.getTopicId(), 2);
            } else {
                logger.info("帖子审查通过，删除任务，更新帖子状态");
                topicService.updateTopicStatus(task.getTopicId(), 0);
                // 发布消息 添加缓存
                rabbitTemplate.convertAndSend(QueueConstant.INDEX_TOPIC_QUEUE, topic);
                if (topic.getType() == 1) {
                    topicService.addTopicToRedisIdList(task.getTopicId());
                }
            }
            taskService.deleteReviewTask(task.getId());
        });
        return new ProcessResult(true);
    }
}
