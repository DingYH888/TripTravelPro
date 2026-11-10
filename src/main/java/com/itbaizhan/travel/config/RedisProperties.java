package com.itbaizhan.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 双实例连接配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "travel.redis")
public class RedisProperties {
    private Instance cache = new Instance();
    private Instance state = new Instance();

    @Data
    public static class Instance {
        private String host = "127.0.0.1";
        private int port = 6379;
        private int database = 0;
        private String password = "";
        private Duration timeout = Duration.ofSeconds(2);
        private int maxTotal = 32;
        private int maxIdle = 8;
        private int minIdle = 2;
    }
}
