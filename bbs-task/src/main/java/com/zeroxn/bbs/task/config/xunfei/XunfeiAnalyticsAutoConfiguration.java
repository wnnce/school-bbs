package com.zeroxn.bbs.task.config.xunfei;

import okhttp3.OkHttp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 10:55:16
 * @Description:
 */
@Configuration
@ConditionalOnClass(OkHttp.class)
@EnableConfigurationProperties(XunfeiAnalyticsProperties.class)
@Import({ XunfeiAnalyticsConfigurations.ClientConfiguration.class, XunfeiAnalyticsConfigurations.AnalyticsConfiguration.class })
public class XunfeiAnalyticsAutoConfiguration {
}
