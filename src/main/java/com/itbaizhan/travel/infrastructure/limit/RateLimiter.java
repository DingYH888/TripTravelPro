package com.itbaizhan.travel.infrastructure.limit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

/**
 * 固定窗口限流，计数存放在 state 实例，避免被缓存淘汰策略清理。
 */
@Component
public class RateLimiter {
    private static final DefaultRedisScript<Long> INCR_SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('incr', KEYS[1]) "
                    + "if c == 1 then redis.call('pexpire', KEYS[1], ARGV[1]) end "
                    + "return c",
            Long.class);

    private final StringRedisTemplate stateRedisTemplate;

    public RateLimiter(@Qualifier("stateStringRedisTemplate") StringRedisTemplate stateRedisTemplate) {
        this.stateRedisTemplate = stateRedisTemplate;
    }

    public boolean allow(String scene, String identity, int limit, Duration window) {
        String key = "rate:" + scene + ":" + identity;
        Long count = stateRedisTemplate.execute(INCR_SCRIPT, Collections.singletonList(key),
                String.valueOf(window.toMillis()));
        return count != null && count <= limit;
    }
}
