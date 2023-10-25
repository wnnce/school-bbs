package com.zeroxn.bbs.core.config.wechat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.base.cache.CacheService;
import com.zeroxn.bbs.core.common.WechatService;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

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
