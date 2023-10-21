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
