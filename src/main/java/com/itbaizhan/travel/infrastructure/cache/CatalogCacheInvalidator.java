package com.itbaizhan.travel.infrastructure.cache;

import org.springframework.stereotype.Component;

/**
 * 目录缓存失效器：产品/分类写入后清理公开目录缓存，避免最长 10 分钟的脏读。
 */
@Component
public class CatalogCacheInvalidator {
    public static final String KEY_CATEGORIES = "cache:catalog:categories";
    private static final String PATTERN_PRODUCT = "cache:product:*";

    private final CacheService cacheService;

    public CatalogCacheInvalidator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void invalidate() {
        cacheService.evictByPattern(PATTERN_PRODUCT);
        cacheService.evict(KEY_CATEGORIES);
    }
}
