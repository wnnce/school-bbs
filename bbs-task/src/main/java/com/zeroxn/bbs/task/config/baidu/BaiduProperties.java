package com.zeroxn.bbs.task.config.baidu;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:37:13
 * @Description:
 */
@Getter
@ConfigurationProperties(prefix = "baidu")
public class BaiduProperties {
    /**
     * 百度智能云应用Appid
     */
    private final String appId;
    /**
     * 百度智能云应用Appkey
     */
    private final String appKey;
    /**
     * 百度智能云应用Secretkey
     */
    private final String secretKey;
    /**
     * 百度智能云请求AccessToken grant_type参数
     */
    private final String grantType;
    @ConstructorBinding
    public BaiduProperties(String appId, String appKey, String secretKey, String grantType) {
        this.appId = appId;
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.grantType = grantType;
    }
}
