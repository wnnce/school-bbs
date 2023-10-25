package com.zeroxn.bbs.base.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 19:33:47
 * @Description: 基于内存的缓存管理器
 */
public class InMemoryCacheService implements CacheService {
    /**
     * 多线程安全的Map
     */
    private static final Map<String, CacheItem> cahceMap = new ConcurrentHashMap<>();

    /**
     * 添加缓存，将缓存添加到Map对象中
     * @param key 缓存的Key
     * @param value 缓存的value 泛型
     * @param expire 缓存的超时时间
     * @param <T> 泛型
     */
    @Override
    public <T> void setCache(String key, T value, Duration expire) {
        CacheItem cacheItem = new CacheItem(value, System.currentTimeMillis() + expire.toMillis());
        cahceMap.put(key, cacheItem);
    }

    /**
     * 通过key拿到缓存的值，如果key获取为空或者缓存已过有效时间则直接返回空
     * @param key 缓存的Key
     * @param clazz 泛型 返回指定类型的数据
     * @return 返回缓存的数据
     * @param <T> 泛型，用于强制转型
     */
    @Override
    public <T> T getCache(String key, Class<T> clazz) {
        CacheItem cacheItem = cahceMap.get(key);
        if (cacheItem == null) {
            return null;
        }
        if (cacheItem.expireTime() <= System.currentTimeMillis()) {
            this.remove(key);
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
