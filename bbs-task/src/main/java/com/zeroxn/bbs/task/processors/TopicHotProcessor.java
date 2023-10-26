package com.zeroxn.bbs.task.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.task.dao.TopicCalculate;
import com.zeroxn.bbs.task.dao.TopicDao;
import com.zeroxn.bbs.task.service.TopicService;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 20:16:34
 * @Description: 生成热门话题任务
 */
@Component(value = "topicHotProcessor")
public class TopicHotProcessor implements BasicProcessor {
    private final TopicService topicService;
    private final ObjectMapper objectMapper;
    public TopicHotProcessor(TopicService topicService, ObjectMapper objectMapper) {
        this.topicService = topicService;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成热门话题的定时任务，供任务平台调用
     * 热门话题的生成逻辑为：先获取所有状态为正常的话题，再使用流处理对每一个话题进行处理
     * 先拿到话题发布距今的小时数和针对该话题最后一条评论距今的小时数，在将话题的查看次数、收藏次数、评论次数、发布距今小时数、
     * 最后一条评论距今小时数进行归一化处理 (查看次数最大值：100000,收藏次数最大值：10000,评论数、发布小时数、评论小时数：最大值1000)
     * 归一化处理完成后查看次数、收藏次数、评论次数乘对应的权重计算出分数，发布小时数，评论小时数同理
     * 最后相加所有计算得到的分数得到话题的最终热度，按照热度进行倒叙排序取对应的前N作为热门话题
     * @param taskContext 任务平台上下文
     * @return 返回方法的执行结果
     * @throws Exception 异常
     */
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        TaskParam param = objectMapper.readValue(taskContext.getJobParams(), TaskParam.class);
        OmsLogger logger = taskContext.getOmsLogger();
        List<TopicDao> topicList = topicService.listAllTopic();
        if (topicList == null || topicList.isEmpty()) {
            logger.info("获取题列表为空，任务结束。");
            return new ProcessResult(true, "列表为空，任务结束");
        }
        logger.info("获取话题列表成功，话题数量：{}", topicList.size());
        logger.info("获取话题距今的发布和最后评论小时，并对查看次数、收藏次数、评论次数进行归一化处理");
        List<TopicCalculate> topicCalculateList = new ArrayList<>(topicList.stream().map(topic -> {
            LocalDateTime now = LocalDateTime.now();
            int publishHour = (int) Math.ceil(ChronoUnit.HOURS.between(topic.getCreateTime(), now));
            int lastCommentHour = 0;
            if (topic.getLastCommentTime() != null) {
                lastCommentHour = (int) Math.ceil(ChronoUnit.HOURS.between(topic.getLastCommentTime(), now));
            }
            BigDecimal views = new BigDecimal(0), stars = new BigDecimal(0), comments = new BigDecimal(0);
            if (topic.getViewCount() > 0) {
                views = new BigDecimal(topic.getViewCount()).divide(BigDecimal.valueOf(100000), 5, RoundingMode.HALF_UP);
            }
            if (topic.getStarCount() > 0) {
                stars = new BigDecimal(topic.getStarCount()).divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
            }
            if (topic.getCommentCount() > 0) {
                comments = new BigDecimal(topic.getCommentCount()).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            }
            return new TopicCalculate(topic.getId(), publishHour, lastCommentHour, views, stars, comments);
        }).toList());
        logger.info("开始计算话题热度");
        topicCalculateList.forEach(calculate -> {
            BigDecimal publishTimeScore = new BigDecimal(calculate.getPublishHour())
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(param.publishTimeWeight()));
            BigDecimal commentTimeScore = new BigDecimal(calculate.getLastCommentHour())
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(param.commentTimeWeight()));
            BigDecimal viewsScore = calculate.getViews().multiply(BigDecimal.valueOf(param.viewWeight()));
            BigDecimal starsScore = calculate.getStars().multiply(BigDecimal.valueOf(param.starWeight()));
            BigDecimal commentsScore = calculate.getComments().multiply(BigDecimal.valueOf(param.commentWeight()));
            double heatLevel = publishTimeScore.add(commentTimeScore).add(viewsScore).add(starsScore).add(commentsScore).doubleValue();
            calculate.setHeatLevel(heatLevel);
        });
        logger.info("话题热度计算完毕，开始根据热度排序");
        if (topicCalculateList.size() > 1) {
            topicCalculateList.sort(Collections.reverseOrder(TopicCalculate.calculateComparator));
        }else {
            logger.info("话题数量小于2，跳过排序");
        }
        Double maxHeat = topicCalculateList.get(0).getHeatLevel();
        Double minHeat = topicCalculateList.get(topicCalculateList.size() - 1).getHeatLevel();
        logger.info("话题最高热度：{}，最低热度：{}", maxHeat, minHeat);

        List<Integer> topicHotIdList = null;

        if (topicCalculateList.size() > param.maxSize()) {
            topicHotIdList = topicCalculateList.subList(0, param.maxSize()).stream().map(TopicCalculate::getId).toList();
        }else {
            logger.info("话题数量不足{}条，不做截取", param.maxSize());
            topicHotIdList = topicCalculateList.stream().map(TopicCalculate::getId).toList();
        }
        logger.info("开始将计算结果写入数据库");
        boolean result = topicService.updateTopicHot(topicHotIdList);
        if (result) {
            logger.info("写入数据库成功");
            return new ProcessResult(true, null);
        }
        return new ProcessResult(false, "写入数据库失败");

    }
    private record TaskParam(Double publishTimeWeight, Double commentTimeWeight, Double viewWeight, Double starWeight,
                             Double commentWeight, Integer maxSize) {}
}
