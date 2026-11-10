package com.itbaizhan.travel.config;

import io.lettuce.core.ClientOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

    @Bean(name = "cacheRedisConnectionFactory")
    @Primary
    public LettuceConnectionFactory cacheRedisConnectionFactory(RedisProperties properties) {
        return buildFactory(properties.getCache());
    }

    @Bean(name = "stateRedisConnectionFactory")
    public LettuceConnectionFactory stateRedisConnectionFactory(RedisProperties properties) {
        return buildFactory(properties.getState());
    }

    @Bean(name = "cacheRedisTemplate")
    @Primary
    public RedisTemplate<String, Object> cacheRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory factory) {
        return buildTemplate(factory);
    }

    @Bean(name = "stateRedisTemplate")
    public RedisTemplate<String, Object> stateRedisTemplate(
            @Qualifier("stateRedisConnectionFactory") RedisConnectionFactory factory) {
        return buildTemplate(factory);
    }

    @Bean(name = "cacheStringRedisTemplate")
    @Primary
    public StringRedisTemplate cacheStringRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean(name = "stateStringRedisTemplate")
    public StringRedisTemplate stateStringRedisTemplate(
            @Qualifier("stateRedisConnectionFactory") RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    private LettuceConnectionFactory buildFactory(RedisProperties.Instance instance) {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(instance.getHost());
        standalone.setPort(instance.getPort());
        standalone.setDatabase(instance.getDatabase());
        if (StringUtils.hasText(instance.getPassword())) {
            standalone.setPassword(RedisPassword.of(instance.getPassword()));
        }

        GenericObjectPoolConfig<?> pool = new GenericObjectPoolConfig<>();
        pool.setMaxTotal(instance.getMaxTotal());
        pool.setMaxIdle(instance.getMaxIdle());
        pool.setMinIdle(instance.getMinIdle());

        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(instance.getTimeout())
                .clientOptions(ClientOptions.builder().autoReconnect(true).build())
                .poolConfig(pool)
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientConfig);
        factory.afterPropertiesSet();
        return factory;
    }

    private RedisTemplate<String, Object> buildTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
