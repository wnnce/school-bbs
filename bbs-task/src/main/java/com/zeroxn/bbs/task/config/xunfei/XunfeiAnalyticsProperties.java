package com.zeroxn.bbs.task.config.xunfei;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 10:48:09
 * @Description:
 */
@Getter
@ConfigurationProperties(prefix = "xunfei.analytics")
public class XunfeiAnalyticsProperties {
    private final String appid;
    private final String apiKey;
    private final String type;
    private final String requestUrl;
    @ConstructorBinding
    public XunfeiAnalyticsProperties(String appid, String apiKey, String type, String requestUrl) {
        this.appid = appid;
        this.type = type;
        this.apiKey = apiKey;
        this.requestUrl = requestUrl;
    }
}