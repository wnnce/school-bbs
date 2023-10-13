package com.zeroxn.bbs.core.config.security;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 14:53:45
 * @Description: Jwt配置绑定类
 */
@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * Token有效时间
     */
    private final Duration expireTime;
    /**
     * Token的预期接受者
     */
    private final List<String> audience;
    /**
     * Token的发行人
     */
    private final String issuer;
    /**
     * 是否使用随机生成的证书
     */
    private final Boolean randomKey;
    /**
     * 自定义公钥证书路径
     */
    private final String publicKeyPath;
    /**
     * 自定义私钥证书路径
     */
    private final String privateKeyPath;
    @ConstructorBinding
    public JwtProperties(@DefaultValue("24") Integer expireHours, List<String> audience, String issuer,@DefaultValue("true") Boolean randomKey,
                         String publicKeyPath, String privateKeyPath) {
        this.expireTime = Duration.ofHours(expireHours);
        this.audience = audience;
        this.issuer = issuer;
        this.randomKey = randomKey;
        this.publicKeyPath = publicKeyPath;
        this.privateKeyPath = privateKeyPath;
    }
}
