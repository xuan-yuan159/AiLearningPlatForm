package org.xuanyuan.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xuanyuan.auth.dto.LoginRequest;
import org.xuanyuan.auth.dto.LoginResponse;
import org.xuanyuan.auth.service.AuthService;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(UserContext.getUserId());
        return Result.success();
    }
}
