package com.zeroxn.bbs.core.config.qiniu;

import com.qiniu.util.Auth;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:14:44
 * @Description: 七牛云上传自动配置类
 */
@Configuration
@ConditionalOnClass(Auth.class)
@EnableConfigurationProperties(QiniuProperties.class)
@Import({ QiniuConfigurations.QiniuAuthConfiguration.class, QiniuConfigurations.QiniuServiceConfiguration.class })
public class QiniuAutoConfiguration {
}
