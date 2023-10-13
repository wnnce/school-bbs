package com.zeroxn.bbs.core.config.qiniu;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:09:51
 * @Description: 七牛云配置参数类
 */
@Getter
@ConfigurationProperties(prefix = "qiniu")
public class QiniuProperties {

    private final String accessKey;
    private final String secretKey;

    private final String bucket;
    private final String bucketDomain;

    private final String imageFolder;
    private final String videoFolder;

    @ConstructorBinding
    public QiniuProperties(String accessKey, String secretKey, String bucket, String bucketDomain,
                           @DefaultValue("bbs/images/") String imageFolder, @DefaultValue("bbs/videos") String videoFolder) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.bucketDomain = bucketDomain;
        if (!imageFolder.endsWith("/")) {
            imageFolder = imageFolder + "/";
        }
        this.imageFolder = imageFolder;
        if (!videoFolder.endsWith("/")) {
            videoFolder = videoFolder + "/";
        }
        this.videoFolder = videoFolder;
    }
}
