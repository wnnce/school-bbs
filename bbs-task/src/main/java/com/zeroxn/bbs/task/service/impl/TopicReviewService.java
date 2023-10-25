package com.zeroxn.bbs.task.service.impl;

import com.zeroxn.bbs.task.filter.ContentSecurityReview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 13:14:00
 * @Description:
 */
@Service
public class TopicReviewService {
    private static final Logger logger = LoggerFactory.getLogger(TopicReviewService.class);
    private final ContentSecurityReview securityReview;
    public TopicReviewService(ContentSecurityReview securityReview) {
        this.securityReview = securityReview;
    }

    public CompletableFuture<Optional<Boolean>> asyncReviewTopicText(String text) {
        return CompletableFuture.supplyAsync(() -> {
            Boolean result = securityReview.filterText(text);
            return Optional.ofNullable(result);
        });
    }

    /**
     * 审查帖子的视频
     * @param videoUrl 帖子的视频链接
     * @return 使用Optional封装返回参数 使之可以返回null
     */
    public CompletableFuture<Optional<Boolean>> asyncReviewTopicVideo(String videoUrl) {
        return CompletableFuture.supplyAsync(() -> {
            if (videoUrl == null || videoUrl.isEmpty()) {
                return Optional.of(true);
            }
            String videoName = videoUrl.substring(videoUrl.lastIndexOf('/'));
            Boolean result = securityReview.filterVideo(UUID.randomUUID().toString(), videoName, videoUrl);
            return Optional.ofNullable(result);
        });
    }

    /**
     * 审查帖子/话题中所有的图片，采用异步线程同步审查，其中有一张图为false  整个方法返回false 有一种图片调用失败 整个方法返回null
     * @param imageUrls 图片数组
     * @return 使用Optional包装返回类 使之可以返回null
     */
    public CompletableFuture<Optional<Boolean>> asyncReviewTopicImage(String[] imageUrls) {
        return CompletableFuture.supplyAsync(() -> {
            if (imageUrls == null || imageUrls.length == 0) {
                return Optional.of(true);
            }
            List<String> imageUrlList = Arrays.stream(imageUrls).toList();
            List<CompletableFuture<Boolean>> imageResultFutureList = new ArrayList<>();
            imageUrlList.forEach(imageUrl -> {
                CompletableFuture<Boolean> imageResultFuture = CompletableFuture.supplyAsync(() -> securityReview.filterImage(imageUrl));
                imageResultFutureList.add(imageResultFuture);
            });
            try {
                CompletableFuture.allOf(imageResultFutureList.toArray(CompletableFuture[]::new)).get();
                for (CompletableFuture<Boolean> cf : imageResultFutureList) {
                    Boolean result = cf.get();
                    if (result == null || !result) {
                        return Optional.ofNullable(result);
                    }
                }
                return Optional.of(true);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("等待图片审查线程结束异常，错误信息：{}", e.getMessage());
            }
            return Optional.empty();
        });
    }
}
