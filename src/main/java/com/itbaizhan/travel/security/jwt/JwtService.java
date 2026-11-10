package com.itbaizhan.travel.security.jwt;

import com.itbaizhan.travel.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_MEMBER = "member";

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_PREFIX = "jwt:refresh:";
    private static final String REFRESH_VALUE_SEPARATOR = "|";

    private final JwtProperties properties;
    private final StringRedisTemplate stateRedisTemplate;
    private final Key key;

    public JwtService(JwtProperties properties,
                      @Qualifier("stateStringRedisTemplate") StringRedisTemplate stateRedisTemplate) {
        this.properties = properties;
        this.stateRedisTemplate = stateRedisTemplate;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken issue(String typ, Integer uid, String username, List<String> permissions) {
        Duration ttl = TYPE_ADMIN.equals(typ) ? properties.getAdminAccessTtl() : properties.getMemberAccessTtl();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttl.toMillis());
        String jti = UUID.randomUUID().toString();

        String accessToken = Jwts.builder()
                .setIssuer(properties.getIssuer())
                .setSubject(username)
                .setId(jti)
                .claim("uid", uid)
                .claim("typ", typ)
                .claim("perms", permissions == null ? new ArrayList<String>() : permissions)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = createRefreshToken(typ, uid, username, permissions);
        return new IssuedToken(accessToken, refreshToken, ttl.getSeconds());
    }

    public String createRefreshToken(String typ, Integer uid, String username, List<String> permissions) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        String perms = permissions == null ? "" : String.join(",", permissions);
        String value = typ + REFRESH_VALUE_SEPARATOR + uid + REFRESH_VALUE_SEPARATOR + username
                + REFRESH_VALUE_SEPARATOR + perms;
        stateRedisTemplate.opsForValue().set(REFRESH_PREFIX + refreshToken, value, properties.getRefreshTtl());
        return refreshToken;
    }

    /**
     * 刷新令牌并轮换：旧 refreshToken 立即失效。
     */
    public IssuedToken refresh(String refreshToken) {
        String key = REFRESH_PREFIX + refreshToken;
        String value = stateRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            throw new JwtException("refresh token invalid");
        }
        stateRedisTemplate.delete(key);

        String[] parts = value.split("\\|", -1);
        if (parts.length < 3) {
            throw new JwtException("refresh token malformed");
        }
        String type = parts[0];
        if (!TYPE_ADMIN.equals(type) && !TYPE_MEMBER.equals(type)) {
            throw new JwtException("refresh token type invalid");
        }
        Integer uid = Integer.valueOf(parts[1]);
        String username = parts[2];
        List<String> permissions = new ArrayList<>();
        if (parts.length >= 4 && StringUtils.hasText(parts[3])) {
            for (String perm : parts[3].split(",")) {
                if (StringUtils.hasText(perm)) {
                    permissions.add(perm);
                }
            }
        }
        return issue(type, uid, username, permissions);
    }

    public JwtPrincipal parseAccess(String token, String expectedType) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String typ = claims.get("typ", String.class);
        if (!expectedType.equals(typ)) {
            throw new JwtException("token type mismatch");
        }
        Integer uid = claims.get("uid", Integer.class);
        String jti = claims.getId();
        if (uid == null || !StringUtils.hasText(jti)) {
            throw new JwtException("token missing claims");
        }
        List<String> permissions = new ArrayList<>();
        Object perms = claims.get("perms");
        if (perms instanceof List) {
            for (Object perm : (List<?>) perms) {
                permissions.add(String.valueOf(perm));
            }
        }
        return new JwtPrincipal(uid, claims.getSubject(), typ, jti, permissions);
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(stateRedisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    /**
     * 主动失效 access token，TTL 与令牌剩余时间一致。
     */
    public void blacklist(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            long ttlMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttlMillis > 0) {
                stateRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + claims.getId(), "1",
                        Duration.ofMillis(ttlMillis));
            }
        } catch (Exception ignored) {
            // 已过期或非法 token 无需拉黑
        }
    }

    public void revokeRefreshToken(String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            stateRedisTemplate.delete(REFRESH_PREFIX + refreshToken);
        }
    }

    public static class IssuedToken {
        private final String accessToken;
        private final String refreshToken;
        private final long expiresIn;

        public IssuedToken(String accessToken, String refreshToken, long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }
    }
}
