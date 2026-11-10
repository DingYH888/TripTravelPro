package com.itbaizhan.travel.infrastructure.lock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 基于 state 实例的轻量分布式锁（SET NX PX + Lua 安全释放）。
 */
@Component
public class RedisLock {
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stateRedisTemplate;

    public RedisLock(@Qualifier("stateStringRedisTemplate") StringRedisTemplate stateRedisTemplate) {
        this.stateRedisTemplate = stateRedisTemplate;
    }

    public String tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean success = stateRedisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(success) ? token : null;
    }

    public void unlock(String key, String token) {
        if (token == null) {
            return;
        }
        stateRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
    }
}
