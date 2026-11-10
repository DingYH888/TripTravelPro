package com.itbaizhan.travel.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.pojo.Permission;
import com.itbaizhan.travel.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
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

/**
 * 后台权限管理接口。
 */
@RestController
@RequestMapping("/api/v1/admin/permissions")
@PreAuthorize("hasAuthority('/permission/all')")
public class AdminPermissionController {
    private final PermissionService permissionService;

    public AdminPermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<PageResult<Permission>> list(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Permission> result = permissionService.findPage(safePage, safeSize);
        return ApiResponse.ok(PageResult.of(result));
    }

    @GetMapping("/{pid}")
    public ApiResponse<Permission> detail(@PathVariable Integer pid) {
        Permission permission = permissionService.findById(pid);
        if (permission == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        return ApiResponse.ok(permission);
    }

    @PostMapping
    public ApiResponse<Permission> create(@RequestBody Permission permission) {
        validate(permission);
        permission.setPid(null);
        permissionService.add(permission);
        return ApiResponse.ok(permission);
    }

    @PutMapping("/{pid}")
    public ApiResponse<Void> update(@PathVariable Integer pid, @RequestBody Permission permission) {
        if (permissionService.findById(pid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        validate(permission);
        permission.setPid(pid);
        permissionService.update(permission);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{pid}")
    public ApiResponse<Void> delete(@PathVariable Integer pid) {
        if (permissionService.findById(pid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        permissionService.delete(pid);
        return ApiResponse.ok();
    }

    private void validate(Permission permission) {
        if (!StringUtils.hasText(permission.getPermissionName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "权限名称不能为空");
        }
        if (!StringUtils.hasText(permission.getPermissionDesc())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "权限标识不能为空");
        }
    }
}
