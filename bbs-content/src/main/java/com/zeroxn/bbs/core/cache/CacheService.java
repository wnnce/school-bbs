package com.zeroxn.bbs.core.cache;

import java.time.Duration;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 19:29:05
 * @Description: 简易缓存管理
 */
public interface CacheService {
    /**
     * 添加缓存
     * @param key 缓存的Key
     * @param value 缓存的value 泛型
     * @param expire 缓存的超时时间
     * @param <T> 泛型
     */
    <T> void setCache(String key, T value, Duration expire);

    /**
     * 获取缓存
     * @param key 缓存的Key
     * @param clazz 泛型 返回指定类型的数据
     * @return 返回缓存数据或空
     * @param <T> 泛型 返回指定类型的数据
     */
    <T> T getCache(String key, Class<T> clazz);

    /**
     * 删除缓存
     * @param key 缓存的key
     */
    void remove(String key);
}
