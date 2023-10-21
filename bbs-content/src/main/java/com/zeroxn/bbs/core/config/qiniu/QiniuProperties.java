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
    /**
     * 七牛云的accessToken
     */
    private final String accessKey;
    /**
     * 七牛云的secretToken
     */
    private final String secretKey;
    /**
     * 七牛云中的存储桶名称
     */
    private final String bucket;
    /**
     * 存储桶的域名
     */
    private final String bucketDomain;
    /**
     * 保存图片的自定义文件夹
     */
    private final String imageFolder;
    /**
     * 保存视频的自定义文件夹
     */
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
