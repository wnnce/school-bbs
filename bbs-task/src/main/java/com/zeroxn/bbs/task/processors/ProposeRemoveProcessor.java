package com.zeroxn.bbs.task.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.task.service.TopicService;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: lisang
 * @DateTime: 2023-11-02 11:20:02
 * @Description: 用户推荐数据删除任务，定时删除超时一定时间的推荐话题
 * 通过相关度进行匹配，相关度越高留存时间越长
 */
@Component("proposeRemoveProcessor")
public class ProposeRemoveProcessor implements BasicProcessor {
    private final TopicService topicService;
    private final ObjectMapper objectMapper;
    public ProposeRemoveProcessor(TopicService topicService, ObjectMapper objectMapper) {
        this.topicService = topicService;
        this.objectMapper = objectMapper;
    }
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        OmsLogger logger = taskContext.getOmsLogger();
        String jobParams = taskContext.getJobParams();
        TaskParams params;
        try {
            params = objectMapper.readValue(jobParams, TaskParams.class);
        }catch (JsonProcessingException ex) {
            logger.error("任务参数反序列失败，错误信息：{}", ex.getMessage());
            return new ProcessResult(false);
        }
        logger.info("开始处理用户推荐列表删除");
        List<Long> userIdList = topicService.listProposeTopicUserid();
        if (userIdList == null || userIdList.isEmpty()) {
            logger.warn("获取到的推荐表用户ID列表为空，任务结束");
            return new ProcessResult(true);
        }
        List<List<Long>> batches = new ArrayList<>();
        if (userIdList.size() > params.maxHandler()) {
            logger.info("用户列表长度超多最大并发处理限制，启用分批处理");
            for (int i = 0; i < userIdList.size(); i += params.maxHandler()) {
                int endIndex = Math.min(i + params.maxHandler, userIdList.size());
                List<Long> subList = userIdList.subList(i, endIndex);
                batches.add(subList);
            }
        }else {
            batches.add(userIdList);
        }
        logger.info("用户列表分批处理完成，批次：{}", batches.size());
        batches.forEach(idList -> {
            List<CompletableFuture<Void>> futureList = new ArrayList<>();
            idList.forEach(userId -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    int result = topicService.deleteUserOldPropose(userId, params.limit());
                    logger.info("删除用户：{} 的旧推荐记录完成，删除记录数：{}", userId, result);
                });
                futureList.add(future);
            });
            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).join();
        });
        logger.info("处理用户旧推荐数据完毕");
        return new ProcessResult(true);
    }
    private record TaskParams(Integer maxHandler, Integer limit) {}
}
