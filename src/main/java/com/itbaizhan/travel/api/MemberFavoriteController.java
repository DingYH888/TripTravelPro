package com.itbaizhan.travel.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.api.PageResult;
import com.itbaizhan.travel.pojo.Product;
import com.itbaizhan.travel.security.jwt.JwtPrincipal;
import com.itbaizhan.travel.service.FavoriteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member/favorites")
public class MemberFavoriteController {
    private final FavoriteService favoriteService;

    public MemberFavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<PageResult<Product>> list(@AuthenticationPrincipal JwtPrincipal principal,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<Product> result = favoriteService.findMemberFavorite(safePage, safeSize, principal.getUid());
        return ApiResponse.ok(PageResult.of(result));
    }

    @GetMapping("/{pid}/status")
    public ApiResponse<Boolean> status(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Integer pid) {
        return ApiResponse.ok(favoriteService.findFavorite(pid, principal.getUid()));
    }

    @PostMapping("/{pid}")
    public ApiResponse<Boolean> add(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Integer pid) {
        if (!favoriteService.findFavorite(pid, principal.getUid())) {
            favoriteService.addFavorite(pid, principal.getUid());
        }
        return ApiResponse.ok(true);
    }

    @DeleteMapping("/{pid}")
    public ApiResponse<Boolean> delete(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Integer pid) {
        favoriteService.delFavorite(pid, principal.getUid());
        return ApiResponse.ok(false);
    }
}
