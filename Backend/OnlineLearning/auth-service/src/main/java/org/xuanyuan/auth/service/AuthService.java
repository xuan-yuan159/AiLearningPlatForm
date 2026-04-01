package org.xuanyuan.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.xuanyuan.auth.dto.LoginRequest;
import org.xuanyuan.auth.dto.LoginResponse;
import org.xuanyuan.common.util.JwtUtils;
import org.xuanyuan.common.exception.BaseException;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    public LoginResponse login(LoginRequest request) {
        // TODO: Validate username/password against database
        // For now, mock a successful login for any user
        Long userId = 1L; // Mock ID
        String role = "STUDENT"; // Mock Role

        if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            userId = 100L;
            role = "ADMIN";
        } else if (request.getUsername() == null || request.getPassword() == null) {
            throw new BaseException(400, "Username and password are required");
        }

        String token = jwtUtils.createToken(userId, role, new HashMap<>());

        // Cache JWT in Redis: user:{userId} -> token, TTL = token expiration + 5 mins
        String redisKey = "user:" + userId;
        redisTemplate.opsForValue().set(redisKey, token, jwtUtils.getExpiration() + 300000, TimeUnit.MILLISECONDS);

        return new LoginResponse(token, userId, role);
    }

    public void logout(Long userId) {
        redisTemplate.delete("user:" + userId);
    }
}
