package com.itbaizhan.travel.api;

import com.itbaizhan.travel.api.dto.CaptchaResponse;
import com.itbaizhan.travel.api.dto.LoginRequest;
import com.itbaizhan.travel.api.dto.RefreshRequest;
import com.itbaizhan.travel.api.dto.RegisterRequest;
import com.itbaizhan.travel.api.dto.TokenResponse;
import com.itbaizhan.travel.bean.Result;
import com.itbaizhan.travel.common.api.ApiResponse;
import com.itbaizhan.travel.common.exception.BizException;
import com.itbaizhan.travel.common.exception.ErrorCode;
import com.itbaizhan.travel.infrastructure.captcha.CaptchaService;
import com.itbaizhan.travel.infrastructure.limit.RateLimiter;
import com.itbaizhan.travel.pojo.Admin;
import com.itbaizhan.travel.pojo.Member;
import com.itbaizhan.travel.pojo.Permission;
import com.itbaizhan.travel.security.jwt.JwtService;
import com.itbaizhan.travel.service.AdminService;
import com.itbaizhan.travel.service.MemberService;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AdminService adminService;
    private final MemberService memberService;
    private final JwtService jwtService;
    private final RateLimiter rateLimiter;
    private final CaptchaService captchaService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(AdminService adminService, MemberService memberService, JwtService jwtService,
                          RateLimiter rateLimiter, CaptchaService captchaService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.memberService = memberService;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
        this.captchaService = captchaService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/admin/login")
    public ApiResponse<TokenResponse> adminLogin(@Valid @RequestBody LoginRequest request,
                                                 HttpServletRequest servletRequest) {
        checkRate("login:admin", request.getUsername() + ":" + clientIp(servletRequest), 5, Duration.ofMinutes(1));

        Admin admin = adminService.findByAdminName(request.getUsername());
        if (admin == null || !admin.isStatus() || !StringUtils.hasText(admin.getPassword())
                || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        List<String> permissions = adminService.findAllPermission(admin.getUsername()).stream()
                .map(Permission::getPermissionDesc)
                .collect(Collectors.toList());
        JwtService.IssuedToken token = jwtService.issue(JwtService.TYPE_ADMIN, admin.getAid(),
                admin.getUsername(), permissions);
        return ApiResponse.ok(toResponse(token, admin.getUsername(), permissions));
    }

    @PostMapping("/member/login")
    public ApiResponse<TokenResponse> memberLogin(@Valid @RequestBody LoginRequest request,
                                                  HttpServletRequest servletRequest) {
        checkRate("login:member", request.getUsername() + ":" + clientIp(servletRequest), 10, Duration.ofMinutes(1));

        Result result = memberService.login(request.getUsername(), request.getPassword());
        if (!result.isFlag()) {
            throw new BizException(ErrorCode.UNAUTHORIZED, result.getMessage());
        }
        Member member = (Member) result.getData();
        JwtService.IssuedToken token = jwtService.issue(JwtService.TYPE_MEMBER, member.getMid(),
                member.getUsername(), new ArrayList<>());
        return ApiResponse.ok(toResponse(token, member.getUsername(), new ArrayList<>()));
    }

    @PostMapping("/member/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        checkRate("register:ip", clientIp(servletRequest), 5, Duration.ofMinutes(10));
        if (!captchaService.verify(request.getCheckCodeKey(), request.getCheckCode())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
        }
        Member member = new Member();
        BeanUtils.copyProperties(request, member);
        Result result = memberService.register(member);
        if (!result.isFlag()) {
            throw new BizException(ErrorCode.BAD_REQUEST, result.getMessage());
        }
        return ApiResponse.ok();
    }

    @GetMapping("/member/activate")
    public ApiResponse<String> activate(@RequestParam("code") String code) {
        return ApiResponse.ok(memberService.active(code));
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        return ApiResponse.ok(captchaService.generate());
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            JwtService.IssuedToken token = jwtService.refresh(request.getRefreshToken());
            return ApiResponse.ok(toResponse(token, null, null));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody(required = false) RefreshRequest request,
                                    HttpServletRequest servletRequest) {
        String header = servletRequest.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            jwtService.blacklist(header.substring(7));
        }
        if (request != null) {
            jwtService.revokeRefreshToken(request.getRefreshToken());
        }
        return ApiResponse.ok();
    }

    private void checkRate(String scene, String identity, int limit, Duration window) {
        if (!rateLimiter.allow(scene, identity, limit, window)) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private TokenResponse toResponse(JwtService.IssuedToken token, String username, List<String> permissions) {
        TokenResponse response = new TokenResponse();
        response.setAccessToken(token.getAccessToken());
        response.setRefreshToken(token.getRefreshToken());
        response.setExpiresIn(token.getExpiresIn());
        response.setUsername(username);
        response.setPermissions(permissions);
        return response;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
