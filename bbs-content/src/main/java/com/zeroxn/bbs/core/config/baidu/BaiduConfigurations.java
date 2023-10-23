package com.zeroxn.bbs.core.config.baidu;

import com.zeroxn.bbs.core.cache.CacheService;
import com.zeroxn.bbs.core.common.BaiduService;
import org.springframework.context.annotation.Bean;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:43:04
 * @Description:
 */
public class BaiduConfigurations {
    @Bean
    BaiduService baiduService(BaiduProperties properties, CacheService cacheService) {
        return new BaiduService(properties, cacheService);
    }
}
