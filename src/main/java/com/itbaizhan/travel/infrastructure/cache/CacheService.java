package com.itbaizhan.travel.infrastructure.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;

/**
 * 缓存实例（6379）访问封装，所有 key 都带 TTL。
 */
@Service
public class CacheService {
    /**
     * 空值缓存哨兵，防止缓存穿透。
     */
    public static final String NULL_MARKER = "__NULL__";

    private final RedisTemplate<String, Object> cacheRedisTemplate;

    public CacheService(@Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> cacheRedisTemplate) {
        this.cacheRedisTemplate = cacheRedisTemplate;
    }

    public Object get(String key) {
        return cacheRedisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value == null || isNullMarker(value)) {
            return null;
        }
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public boolean isNullCached(String key) {
        return isNullMarker(get(key));
    }

    private boolean isNullMarker(Object value) {
        return NULL_MARKER.equals(value);
    }

    public void set(String key, Object value, Duration ttl) {
        cacheRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public void setNull(String key, Duration ttl) {
        cacheRedisTemplate.opsForValue().set(key, NULL_MARKER, ttl);
    }

    public void evict(String key) {
        cacheRedisTemplate.delete(key);
    }

    public long evictByPattern(String pattern) {
        Set<String> keys = cacheRedisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        Long removed = cacheRedisTemplate.delete(keys);
        return removed == null ? 0L : removed;
    }

    public boolean setIfAbsent(String key, String value, Duration ttl) {
        Boolean result = cacheRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(result);
    }
}
