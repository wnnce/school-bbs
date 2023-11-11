package com.zeroxn.bbs.core.solr.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 20:00:26
 * @Description: Solr配置数据类
 */
@Getter
@ConfigurationProperties(prefix = "solr")
public class SolrProperties {
    /**
     * Solr的地址
     */
    private final String address;
    /**
     * Solr的Http连接超时时间
     */
    private final Integer httpTimeOut;
    /**
     * Solr的Socket连接超时时间
     */
    private final Integer socketTimeOut;

    @ConstructorBinding
    public SolrProperties(String address, @DefaultValue("10000") Integer httpTimeOut,
                          @DefaultValue("5000") Integer socketTimeOut) {
        this.address = address;
        this.httpTimeOut = httpTimeOut;
        this.socketTimeOut = socketTimeOut;
    }
}
