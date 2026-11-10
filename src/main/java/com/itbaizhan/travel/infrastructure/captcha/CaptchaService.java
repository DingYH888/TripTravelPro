package com.itbaizhan.travel.infrastructure.captcha;

import com.itbaizhan.travel.api.dto.CaptchaResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * 图形验证码：状态存 state 实例，5 分钟过期，校验后立即删除。
 */
@Service
public class CaptchaService {
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String PREFIX = "captcha:";
    private static final Duration TTL = Duration.ofMinutes(5);
    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate stateRedisTemplate;

    public CaptchaService(@Qualifier("stateStringRedisTemplate") StringRedisTemplate stateRedisTemplate) {
        this.stateRedisTemplate = stateRedisTemplate;
    }

    public CaptchaResponse generate() {
        String code = randomCode(4);
        String key = UUID.randomUUID().toString().replace("-", "");
        stateRedisTemplate.opsForValue().set(PREFIX + key, code, TTL);
        return new CaptchaResponse(key, "data:image/png;base64," + render(code));
    }

    public boolean verify(String key, String code) {
        if (key == null || code == null || code.trim().isEmpty()) {
            return false;
        }
        String redisKey = PREFIX + key;
        String expected = stateRedisTemplate.opsForValue().get(redisKey);
        stateRedisTemplate.delete(redisKey);
        return expected != null && expected.equalsIgnoreCase(code.trim());
    }

    private String randomCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    private String render(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, width, height);
            for (int i = 0; i < 6; i++) {
                graphics.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
                graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
            }
            graphics.setFont(new Font("Arial", Font.BOLD, 26));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(new Color(30 + random.nextInt(120), 30 + random.nextInt(120), 30 + random.nextInt(120)));
                graphics.drawString(String.valueOf(code.charAt(i)), 12 + i * 26, 29 + random.nextInt(5));
            }
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("captcha render failed", e);
        }
    }
}
