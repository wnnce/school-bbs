package com.zeroxn.bbs.core.config.qiniu;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.context.annotation.Bean;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:15:56
 * @Description: 七牛云上传配置类
 */
public class QiniuConfigurations {

    static class QiniuAuthConfiguration {
        @Bean
        Auth auth(QiniuProperties properties) {
            return Auth.create(properties.getAccessKey(), properties.getSecretKey());
        }
        @Bean
        Configuration configuration() {
            Configuration configuration = new Configuration(Region.autoRegion());
            configuration.resumableUploadAPIVersion = com.qiniu.storage.Configuration.ResumableUploadAPIVersion.V2;
            // 使用 https
            configuration.useHttpsDomains = true;
            return configuration;
        }
    }
    static class QiniuServiceConfiguration {
        @Bean
        UploadManager uploadManager(Configuration configuration) {
            return new UploadManager(configuration);
        }
        @Bean
        BucketManager bucketManager(Auth auth, Configuration configuration) {
            return new BucketManager(auth, configuration);
        }

    }
}
