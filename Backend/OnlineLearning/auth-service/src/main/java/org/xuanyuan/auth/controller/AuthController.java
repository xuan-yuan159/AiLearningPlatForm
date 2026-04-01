package org.xuanyuan.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xuanyuan.auth.dto.LoginRequest;
import org.xuanyuan.auth.dto.LoginResponse;
import org.xuanyuan.auth.service.AuthService;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;

import org.xuanyuan.auth.dto.RegisterRequest;

@Tag(name = "认证管理", description = "用户登录、注册、登出相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "校验用户名密码并返回 JWT")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "用户注册", description = "创建新用户账号")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(summary = "用户登出", description = "注销当前登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(UserContext.getUserId());
        return Result.success();
    }
}
