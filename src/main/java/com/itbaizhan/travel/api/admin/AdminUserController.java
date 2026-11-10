package com.itbaizhan.travel.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.bean.RoleWithStatus;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.pojo.Admin;
import com.itbaizhan.travel.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.util.List;

/**
 * 后台管理员账号管理接口。
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasAuthority('/admin/all')")
public class AdminUserController {
    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ApiResponse<PageResult<Admin>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Admin> result = adminService.findPage(safePage, safeSize);
        return ApiResponse.ok(PageResult.of(result));
    }

    @GetMapping("/{aid}")
    public ApiResponse<Admin> detail(@PathVariable Integer aid) {
        Admin admin = adminService.findById(aid);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "管理员不存在");
        }
        return ApiResponse.ok(admin);
    }

    @PostMapping
    public ApiResponse<Admin> create(@RequestBody Admin admin) {
        if (!StringUtils.hasText(admin.getUsername())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "用户名不能为空");
        }
        if (!StringUtils.hasText(admin.getPassword())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "密码不能为空");
        }
        admin.setAid(null);
        adminService.add(admin);
        return ApiResponse.ok(admin);
    }

    @PutMapping("/{aid}")
    public ApiResponse<Void> update(@PathVariable Integer aid, @RequestBody Admin admin) {
        Admin existing = adminService.findById(aid);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "管理员不存在");
        }
        if (!StringUtils.hasText(admin.getUsername())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "用户名不能为空");
        }
        admin.setAid(aid);
        if (!StringUtils.hasText(admin.getPassword())) {
            admin.setPassword(existing.getPassword());
        }
        adminService.update(admin);
        return ApiResponse.ok();
    }

    @PatchMapping("/{aid}/status")
    public ApiResponse<Boolean> toggleStatus(@PathVariable Integer aid) {
        Admin admin = adminService.findById(aid);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "管理员不存在");
        }
        adminService.updateStatus(aid);
        return ApiResponse.ok(!admin.isStatus());
    }

    @GetMapping("/{aid}/roles")
    public ApiResponse<List<RoleWithStatus>> roles(@PathVariable Integer aid) {
        if (adminService.findById(aid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "管理员不存在");
        }
        return ApiResponse.ok(adminService.findRole(aid));
    }

    @PutMapping("/{aid}/roles")
    public ApiResponse<Void> updateRoles(@PathVariable Integer aid, @RequestBody(required = false) Integer[] ids) {
        if (adminService.findById(aid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "管理员不存在");
        }
        adminService.updateRoles(aid, ids);
        return ApiResponse.ok();
    }
}
