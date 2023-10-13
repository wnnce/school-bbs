package com.zeroxn.bbs.core.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 19:33:47
 * @Description: 基于内存的缓存管理器
 */
public class MemoryCacheService implements CacheService {

    private static final Map<String, CacheItem> cahceMap = new ConcurrentHashMap<>();
    @Override
    public <T> void setCache(String key, T value, Duration expire) {
        CacheItem cacheItem = new CacheItem(value, System.currentTimeMillis() + expire.toMillis());
        cahceMap.put(key, cacheItem);
    }

    @Override
    public <T> T getCache(String key, Class<T> clazz) {
        CacheItem cacheItem = cahceMap.get(key);
        if (cacheItem == null) {
            return null;
        }
        if (cacheItem.expireTime() <= System.currentTimeMillis()) {
            return null;
        }
        return (T) cacheItem.value();
    }

    @Override
    public void remove(String key) {
        cahceMap.remove(key);
    }
    private record CacheItem(Object value, long expireTime) {}
}
