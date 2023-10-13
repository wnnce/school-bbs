package com.zeroxn.bbs.core.config.wechat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.core.cache.CacheService;
import com.zeroxn.bbs.core.common.WechatService;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:55:58
 * @Description: 微信接口服务配置类
 */
public class WechatConfigurations {

    static class OkHttpClientConfiguration {
        @Bean
        OkHttpClient okHttpClient() {
            return new OkHttpClient.Builder()
                    .callTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(5))
                    .writeTimeout(Duration.ofSeconds(5))
                    .build();
        }
    }

    static class WechatServerConfiguration {
        @Bean
        WechatService wechatService(WechatProperties properties, OkHttpClient client, ObjectMapper objectMapper,
                                    CacheService cacheService) {
            return new WechatService(properties, client, objectMapper, cacheService);
        }
    }
}
