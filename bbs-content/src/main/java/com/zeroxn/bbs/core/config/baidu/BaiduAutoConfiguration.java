package com.zeroxn.bbs.core.config.baidu;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:42:07
 * @Description: 百度智能云自动配置类
 */
@Configuration
@ConditionalOnClass(OkHttpClient.class)
@EnableConfigurationProperties(BaiduProperties.class)
@Import({ BaiduConfigurations.class })
public class BaiduAutoConfiguration {
}
