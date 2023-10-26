package com.zeroxn.bbs.task.processors;

import com.zeroxn.bbs.task.dao.TopicDao;
import com.zeroxn.bbs.task.service.TopicService;
import org.springframework.stereotype.Component;
import scala.Int;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-21 18:46:36
 * @Description: 话题删除任务
 */
@Component(value = "topicDeleteProcessor")
public class TopicDeleteProcessor implements BasicProcessor {
    private final TopicService topicService;
    public TopicDeleteProcessor(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * 话题定时清理定时任务方法
     * 话题清理的逻辑为：先计算出话题发布至今的小时数以及最后一条评论距今的小时数，
     * 然后用话题的查看次数、收藏数和评论数除以发布至今的小时数（查看次数权重过大，取一半）
     * 然后用话题最后一条评论距今的小时数乘 -0.1 ,发布越早影响的权重就越大。
     * 最后用 查看次数权重 + 收藏数权重 + 评论数权重 + 最后一条评论权重 得到话题最终的删除指数，如果该指数小于2.5则将话题删除
     * @param taskContext 任务平台上下文
     * @return 返回方法调用的执行结果
     * @throws Exception 异常
     */
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        OmsLogger logger = taskContext.getOmsLogger();
        List<TopicDao> topicDaoList = topicService.listNotHotTopic();
        if (topicDaoList == null || topicDaoList.isEmpty()) {
            logger.info("待处理的话题列表为空，任务结束");
            return new ProcessResult(true, null);
        }
        logger.info("待处理的话题{}个，开始进行删除权重处理", topicDaoList.size());
        List<Integer> deleteTopicIdList = topicDaoList.stream().filter(topic -> {
            LocalDateTime now = LocalDateTime.now();
            int publishHour = (int) Math.ceil(ChronoUnit.HOURS.between(topic.getCreateTime(), now));
            int lastCommentHour = 0;
            if (topic.getLastCommentTime() != null) {
                lastCommentHour = (int) Math.ceil(ChronoUnit.HOURS.between(topic.getLastCommentTime(), now));
            }
            double viewScore = (double) (topic.getViewCount() / publishHour) * 0.5;
            double starScore = (double) topic.getStarCount() / publishHour;
            double commentScore = (double) topic.getCommentCount() / publishHour;
            double lastCommentScore = lastCommentHour * -0.1;
            return (viewScore + starScore + commentScore + lastCommentScore) < 2.5;
        }).map(TopicDao::getId).toList();
        logger.info("删除话题权重处理完毕，待删除数：{}", deleteTopicIdList.size());
        if (deleteTopicIdList.isEmpty()) {
            logger.info("没有话题符合删除条件，任务结束");
            return new ProcessResult(true, null);
        }
        logger.info("{}个话题待删除，调用批量删除方法", deleteTopicIdList.size());
        boolean result = topicService.deleteTopicByTopicIdList(deleteTopicIdList, logger);
        return new ProcessResult(result, null);
    }
}
