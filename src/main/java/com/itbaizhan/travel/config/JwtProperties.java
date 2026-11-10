package com.itbaizhan.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "travel.jwt")
public class JwtProperties {
    private String issuer = "travel";
    private String secret;
    private Duration adminAccessTtl = Duration.ofHours(2);
    private Duration memberAccessTtl = Duration.ofDays(7);
    private Duration refreshTtl = Duration.ofDays(14);
}
