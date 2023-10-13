package com.zeroxn.bbs.core.config.wechat;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:47:15
 * @Description: 微信接口服务自动配置类
 */
@Configuration
@ConditionalOnClass(OkHttpClient.class)
@EnableConfigurationProperties(WechatProperties.class)
@Import({ WechatConfigurations.OkHttpClientConfiguration.class, WechatConfigurations.WechatServerConfiguration.class })
public class WechatAutoConfiguration {
}