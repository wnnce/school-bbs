package com.zeroxn.bbs.core.config.wechat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.core.cache.CacheService;
import com.zeroxn.bbs.core.common.WechatService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:55:58
 * @Description: 微信接口服务配置类
 */
public class WechatConfigurations {

    @Bean
    WechatService wechatService(WechatProperties properties, OkHttpClient client, ObjectMapper objectMapper,
                                CacheService cacheService) {
        return new WechatService(properties, cacheService);
    }
}
