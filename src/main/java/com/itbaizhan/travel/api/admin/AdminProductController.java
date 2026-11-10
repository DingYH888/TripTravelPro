package com.itbaizhan.travel.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.infrastructure.cache.CatalogCacheInvalidator;
import com.itbaizhan.travel.pojo.Product;
import com.itbaizhan.travel.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 后台产品管理接口。
 */
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {
    private static final List<String> ALLOWED_SUFFIX =
            Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");

    private final ProductService productService;
    private final CatalogCacheInvalidator cacheInvalidator;

    @Value("${upload.path}")
    private String uploadPath;

    public AdminProductController(ProductService productService, CatalogCacheInvalidator cacheInvalidator) {
        this.productService = productService;
        this.cacheInvalidator = cacheInvalidator;
    }

    @GetMapping
    public ApiResponse<PageResult<Product>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Product> result = productService.findPage(safePage, safeSize);
        return ApiResponse.ok(PageResult.of(result));
    }

    @GetMapping("/{pid}")
    public ApiResponse<Product> detail(@PathVariable Integer pid) {
        Product product = productService.findOne(pid);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
        }
        return ApiResponse.ok(product);
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody Product product) {
        validate(product);
        product.setPid(null);
        if (product.getStatus() == null) {
            product.setStatus(Boolean.TRUE);
        }
        productService.add(product);
        cacheInvalidator.invalidate();
        return ApiResponse.ok(product);
    }

    @PutMapping("/{pid}")
    public ApiResponse<Void> update(@PathVariable Integer pid, @RequestBody Product product) {
        if (productService.findOne(pid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
        }
        validate(product);
        product.setPid(pid);
        productService.update(product);
        cacheInvalidator.invalidate();
        return ApiResponse.ok();
    }

    @PatchMapping("/{pid}/status")
    public ApiResponse<Boolean> toggleStatus(@PathVariable Integer pid) {
        Product product = productService.findOne(pid);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "线路不存在");
        }
        productService.updateStatus(pid);
        cacheInvalidator.invalidate();
        return ApiResponse.ok(!product.getStatus());
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择要上传的图片");
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') > -1) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }
        if (!ALLOWED_SUFFIX.contains(suffix)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "只支持上传 jpg/jpeg/png/gif/bmp/webp 格式的图片");
        }

        File dir = new File(uploadPath, "img/product");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("创建上传目录失败：" + dir.getAbsolutePath());
        }
        String filename = UUID.randomUUID() + suffix;
        file.transferTo(new File(dir, filename).getAbsoluteFile());

        String url = "/upload/img/product/" + filename;
        return ApiResponse.ok(Collections.singletonMap("url", url));
    }

    private void validate(Product product) {
        if (!StringUtils.hasText(product.getProductName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "产品名称不能为空");
        }
        if (product.getPrice() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "产品价格不能为空");
        }
        if (product.getCid() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择产品分类");
        }
    }
}
