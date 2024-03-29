package com.zeroxn.bbs.task.config.baidu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.base.cache.CacheService;
import com.zeroxn.bbs.base.cache.InMemoryCacheService;
import com.zeroxn.bbs.task.filter.BaiduContentSecurityReview;
import com.zeroxn.bbs.task.filter.BaiduService;
import com.zeroxn.bbs.task.filter.ContentSecurityReview;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:43:04
 * @Description: 基于百度智能云的内容安全审核配置类
 */
public class BaiduConfigurations {
    static class ClientConfiguration {
        @Bean
        OkHttpClient okHttpClient() {
            return new OkHttpClient.Builder()
                    .callTimeout(Duration.ofMinutes(1))
                    .writeTimeout(Duration.ofMinutes(1))
                    .readTimeout(Duration.ofMinutes(1))
                    .build();
        }
    }
    static class ContentSecurityReviewConfiguration {
        @Bean
        CacheService cacheService() {
            return new InMemoryCacheService();
        }
        @Bean
        BaiduService baiduService(BaiduProperties properties, CacheService cacheService, OkHttpClient client, ObjectMapper objectMapper) {
            return new BaiduService(properties, cacheService, client, objectMapper);
        }
        @Bean
        ContentSecurityReview contentSecurityReview(BaiduService baiduService) {
            return new BaiduContentSecurityReview(baiduService);
        }
    }
}
