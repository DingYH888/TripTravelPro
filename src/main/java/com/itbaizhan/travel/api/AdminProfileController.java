package com.itbaizhan.travel.api;

import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.security.jwt.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminProfileController {

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(@AuthenticationPrincipal JwtPrincipal principal) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("uid", principal.getUid());
        profile.put("username", principal.getUsername());
        profile.put("permissions", principal.getPermissions());
        return ApiResponse.ok(profile);
    }
}
