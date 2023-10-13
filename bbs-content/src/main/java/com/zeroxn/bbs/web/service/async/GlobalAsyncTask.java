package com.zeroxn.bbs.web.service.async;

import com.zeroxn.bbs.core.entity.FileUpload;
import com.zeroxn.bbs.web.mapper.FileUploadMapper;
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
    private static final ExecutorService  executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final FileUploadMapper uploadMapper;

    public GlobalAsyncTask(FileUploadMapper uploadMapper) {
        this.uploadMapper = uploadMapper;
    }

    public void saveFileUploadLog(String md5, String originName, String fileName, Long size, String fileUrl) {
        CompletableFuture.runAsync(() -> {
            FileUpload fileUpload = new FileUpload(md5, originName, fileName, size, fileUrl);
            int result = uploadMapper.insertSelective(fileUpload);
            logger.info("保存文件上传日志，文件名称：{},影响行数：{}", originName, result);
        });
    }
}
