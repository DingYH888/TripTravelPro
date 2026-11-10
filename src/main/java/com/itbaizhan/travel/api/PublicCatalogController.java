package com.itbaizhan.travel.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.infrastructure.cache.CacheService;
import com.itbaizhan.travel.infrastructure.cache.CatalogCacheInvalidator;
import com.itbaizhan.travel.infrastructure.lock.RedisLock;
import com.itbaizhan.travel.pojo.Category;
import com.itbaizhan.travel.pojo.Product;
import com.itbaizhan.travel.service.CategoryService;
import com.itbaizhan.travel.service.ProductService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * 前台公开目录接口：产品列表、详情、分类，带 Redis 缓存与互斥重建。
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicCatalogController {
    private static final String KEY_CATEGORIES = CatalogCacheInvalidator.KEY_CATEGORIES;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CacheService cacheService;
    private final RedisLock redisLock;

    public PublicCatalogController(ProductService productService, CategoryService categoryService,
                                   CacheService cacheService, RedisLock redisLock) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cacheService = cacheService;
        this.redisLock = redisLock;
    }

    @GetMapping("/categories")
    @SuppressWarnings("unchecked")
    public ApiResponse<List<Category>> categories() {
        Object cached = cacheService.get(KEY_CATEGORIES);
        if (cached instanceof List) {
            return ApiResponse.ok((List<Category>) cached);
        }
        List<Category> categories = categoryService.findAll();
        cacheService.set(KEY_CATEGORIES, categories, Duration.ofMinutes(30));
        return ApiResponse.ok(categories);
    }

    @GetMapping("/products")
    @SuppressWarnings("unchecked")
    public ApiResponse<PageResult<Product>> products(@RequestParam(required = false) Integer cid,
                                                     @RequestParam(required = false) String productName,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String key = "cache:product:list:" + (cid == null ? "all" : cid) + ":" + safePage + ":" + safeSize
                + ":" + (StringUtils.hasText(productName) ? Integer.toHexString(productName.hashCode()) : "all");

        Object cached = cacheService.get(key);
        if (cached instanceof PageResult) {
            return ApiResponse.ok((PageResult<Product>) cached);
        }

        Page<Product> result = productService.findProduct(cid, productName, safePage, safeSize);
        PageResult<Product> pageResult = PageResult.of(result);
        cacheService.set(key, pageResult, Duration.ofMinutes(2));
        return ApiResponse.ok(pageResult);
    }

    @GetMapping("/products/{pid}")
    public ApiResponse<Product> detail(@PathVariable Integer pid) {
        String key = "cache:product:detail:" + pid;
        Object cached = cacheService.get(key);
        if (cached instanceof Product) {
            return ApiResponse.ok((Product) cached);
        }
        if (CacheService.NULL_MARKER.equals(cached)) {
            throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
        }

        String lockKey = "lock:product:detail:" + pid;
        String lockToken = redisLock.tryLock(lockKey, Duration.ofSeconds(5));
        if (lockToken == null) {
            return ApiResponse.ok(loadAfterWaiting(key, pid));
        }
        try {
            Product product = productService.findOne(pid);
            if (product == null) {
                cacheService.setNull(key, Duration.ofMinutes(2));
                throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
            }
            cacheService.set(key, product, Duration.ofMinutes(10));
            return ApiResponse.ok(product);
        } finally {
            redisLock.unlock(lockKey, lockToken);
        }
    }

    private Product loadAfterWaiting(String key, Integer pid) {
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(60L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Object value = cacheService.get(key);
            if (value instanceof Product) {
                return (Product) value;
            }
            if (CacheService.NULL_MARKER.equals(value)) {
                throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
            }
        }
        Product product = productService.findOne(pid);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
        }
        return product;
    }
}
