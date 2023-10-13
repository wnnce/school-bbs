package com.zeroxn.bbs.core.config.wechat;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:48:52
 * @Description:
 */

@Getter
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {
    /**
     * 微信小程序appid
     */
    private final String appid;
    /**
     * 微信小程序secret
     */
    private final String secret;
    /**
     * 微信登陆grantType
     */
    private final String loginGrantType;

    /**
     * 微信获取Token grantType
     */
    private final String tokenGrantType;
    /**
     * 微信登录Url
     */
    private final String loginUrl;

    private final String tokenUrl;
    private final String phoneUrl;
    @ConstructorBinding
    public WechatProperties(String appid, String secret, @DefaultValue("authorization_code") String grantType,
                            @DefaultValue("https://api.weixin.qq.com/sns/jscode2session") String loginUrl,
                            @DefaultValue("client_credential") String accessGrantType,
                            @DefaultValue("https://api.weixin.qq.com/cgi-bin/token") String tokenUrl,
                            @DefaultValue("https://api.weixin.qq.com/wxa/business/getuserphonenumber") String phoneUrl) {
        this.appid = appid;
        this.secret = secret;
        this.loginGrantType = grantType;
        this.loginUrl = loginUrl;
        this.tokenGrantType = accessGrantType;
        this.tokenUrl = tokenUrl;
        this.phoneUrl = phoneUrl;
    }
}
