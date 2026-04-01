package org.xuanyuan.auth.service;

import org.xuanyuan.auth.dto.LoginRequest;
import org.xuanyuan.auth.dto.LoginResponse;
import org.xuanyuan.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    void logout(Long userId);
}
