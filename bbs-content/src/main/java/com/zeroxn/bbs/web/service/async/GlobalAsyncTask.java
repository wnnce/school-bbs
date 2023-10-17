package com.zeroxn.bbs.web.service.async;

import com.mybatisflex.core.update.UpdateChain;
import com.zeroxn.bbs.core.entity.*;
import com.zeroxn.bbs.web.mapper.FileUploadMapper;
import com.zeroxn.bbs.web.mapper.ForumTopicMapper;
import com.zeroxn.bbs.web.mapper.UserExtrasMapper;
import com.zeroxn.bbs.web.mapper.UserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:58:13
 * @Description: 全局异步任务类
 */
@Service
public class GlobalAsyncTask {
    private static final Logger logger = LoggerFactory.getLogger(GlobalAsyncTask.class);
    /**
     * 异步任务线程池，线程数量为当前CPU的内核数量
     */
    private static final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final FileUploadMapper uploadMapper;
    private final UserExtrasMapper extrasMapper;
    private final UserProfileMapper profileMapper;
    private final ForumTopicMapper topicMapper;

    public GlobalAsyncTask(FileUploadMapper uploadMapper, UserExtrasMapper extrasMapper, UserProfileMapper profileMapper,
                           ForumTopicMapper topicMapper) {
        this.uploadMapper = uploadMapper;
        this.extrasMapper = extrasMapper;
        this.profileMapper = profileMapper;
        this.topicMapper = topicMapper;
    }

    /**
     * 内部方法，用以运行所有没有放回值的异步方法，使用统一的线程池
     * @param func 函数式接口方法
     */
    private void executeVoidAsyncFunction(Runnable func) {
        CompletableFuture.runAsync(func, executors);
    }

    /**
     * 初始化用户的额外信息
     * @param userId 用户ID
     */
    public void initUserExtraInit(Long userId) {
        this.executeVoidAsyncFunction(() -> {
            UserExtras extras = UserExtras.builder()
                    .userId(userId)
                    .build();
            int result = extrasMapper.insertSelective(extras);
            logger.info("用户额外信息表新增用户记录，添加结果:{}", result);
        });
        this.executeVoidAsyncFunction(() -> {
            UserProfile profile = UserProfile.builder()
                    .userId(userId)
                    .build();
            int result = profileMapper.insertSelective(profile);
            logger.info("用户画像表新增用户记录，添加结果:{}", result);
        });
    }
    public void saveFileUploadLog(String md5, String originName, String fileName, Long size, String fileUrl) {
        this.executeVoidAsyncFunction(() -> {
            FileUpload fileUpload = new FileUpload(md5, originName, fileName, size, fileUrl);
            int result = uploadMapper.insertSelective(fileUpload);
            logger.info("保存文件上传日志，文件名称：{},影响行数：{}", originName, result);
        });
    }

    public void handlerTopicContentKey(ForumTopic topic) {
        this.executeVoidAsyncFunction(() -> {
            logger.info("生成帖子关键字");
        });
    }

    public void handlerTopicPropose(ForumTopic topic) {
        this.executeVoidAsyncFunction(() -> {
            logger.info("处理话题推送");
        });
    }

    public void updateTopicStarCount(Integer topicId, int count, boolean isAdd) {
        String rowOption;
        if (isAdd) {
            rowOption = "+" + count;
        }else {
            rowOption = "-" + count;
        }
        this.executeVoidAsyncFunction(() -> {
            boolean result = UpdateChain.of(ForumTopic.class)
                    .setRaw(ForumTopic::getStarCount, "star_count" + rowOption)
                    .where(ForumTopic::getId).eq(topicId)
                    .update();
            logger.info("更新帖子收藏次数，TopicId：{}, count:{}, isAdd:{},影响行数：{}", topicId, count, isAdd, result);
        });
    }

    public void appendTopicViewCount(Integer topicId, int count) {
        this.executeVoidAsyncFunction(() -> {
            UpdateChain.of(ForumTopic.class)
                    .setRaw(ForumTopic::getViewCount, "view_count + " + count)
                    .where(ForumTopic::getId).eq(topicId)
                    .update();
        });
    }
}
