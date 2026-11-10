package com.itbaizhan.travel.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.bean.PermissionWithStatus;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.pojo.Role;
import com.itbaizhan.travel.service.RoleService;
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

import java.util.List;

/**
 * 后台角色管理接口。
 */
@RestController
@RequestMapping("/api/v1/admin/roles")
@PreAuthorize("hasAuthority('/role/all')")
public class AdminRoleController {
    private final RoleService roleService;

    public AdminRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<PageResult<Role>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Role> result = roleService.findPage(safePage, safeSize);
        return ApiResponse.ok(PageResult.of(result));
    }

    @GetMapping("/{rid}")
    public ApiResponse<Role> detail(@PathVariable Integer rid) {
        Role role = roleService.findById(rid);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return ApiResponse.ok(role);
    }

    @PostMapping
    public ApiResponse<Role> create(@RequestBody Role role) {
        validate(role);
        role.setRid(null);
        roleService.add(role);
        return ApiResponse.ok(role);
    }

    @PutMapping("/{rid}")
    public ApiResponse<Void> update(@PathVariable Integer rid, @RequestBody Role role) {
        if (roleService.findById(rid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        validate(role);
        role.setRid(rid);
        roleService.update(role);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{rid}")
    public ApiResponse<Void> delete(@PathVariable Integer rid) {
        if (roleService.findById(rid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        roleService.delete(rid);
        return ApiResponse.ok();
    }

    @GetMapping("/{rid}/permissions")
    public ApiResponse<List<PermissionWithStatus>> permissions(@PathVariable Integer rid) {
        if (roleService.findById(rid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return ApiResponse.ok(roleService.findPermission(rid));
    }

    @PutMapping("/{rid}/permissions")
    public ApiResponse<Void> updatePermissions(@PathVariable Integer rid, @RequestBody(required = false) Integer[] ids) {
        if (roleService.findById(rid) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        roleService.updatePermissions(rid, ids);
        return ApiResponse.ok();
    }

    private void validate(Role role) {
        if (!StringUtils.hasText(role.getRoleName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "角色名称不能为空");
        }
    }
}
