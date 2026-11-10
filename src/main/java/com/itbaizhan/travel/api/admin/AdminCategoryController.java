package com.itbaizhan.travel.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.infrastructure.cache.CatalogCacheInvalidator;
import com.itbaizhan.travel.pojo.Category;
import com.itbaizhan.travel.service.CategoryService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台产品分类管理接口。
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final CatalogCacheInvalidator cacheInvalidator;

    public AdminCategoryController(CategoryService categoryService, CatalogCacheInvalidator cacheInvalidator) {
        this.categoryService = categoryService;
        this.cacheInvalidator = cacheInvalidator;
    }

    @GetMapping
    public ApiResponse<PageResult<Category>> list(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Category> result = categoryService.findPage(safePage, safeSize);
        return ApiResponse.ok(PageResult.of(result));
    }

    @GetMapping("/all")
    public ApiResponse<List<Category>> all() {
        return ApiResponse.ok(categoryService.findAll());
    }

    @GetMapping("/{cid}")
    public ApiResponse<Category> detail(@PathVariable Integer cid) {
        Category category = categoryService.findById(cid);
        if (category == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return ApiResponse.ok(category);
    }

    @PostMapping
    public ApiResponse<Category> create(@RequestBody Category category) {
        validate(category);
        category.setCid(null);
        categoryService.add(category);
        cacheInvalidator.invalidate();
        return ApiResponse.ok(category);
    }

    @PutMapping("/{cid}")
    public ApiResponse<Void> update(@PathVariable Integer cid, @RequestBody Category category) {
        if (categoryService.findById(cid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        validate(category);
        category.setCid(cid);
        categoryService.update(category);
        cacheInvalidator.invalidate();
        return ApiResponse.ok();
    }

    @DeleteMapping("/{cid}")
    public ApiResponse<Void> delete(@PathVariable Integer cid) {
        if (categoryService.findById(cid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        categoryService.delete(cid);
        cacheInvalidator.invalidate();
        return ApiResponse.ok();
    }

    private void validate(Category category) {
        if (!StringUtils.hasText(category.getCname())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分类名称不能为空");
        }
    }
}
