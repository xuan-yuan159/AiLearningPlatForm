package org.xuanyuan.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.xuanyuan.auth.dto.LoginRequest;
import org.xuanyuan.auth.dto.LoginResponse;
import org.xuanyuan.auth.dto.RegisterRequest;
import org.xuanyuan.auth.entity.User;
import org.xuanyuan.auth.mapper.UserMapper;
import org.xuanyuan.auth.service.AuthService;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.common.util.JwtUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new BaseException(400, "Username and password are required");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BaseException(401, "Invalid username or password");
        }

        if (user.getStatus() == 0) {
            throw new BaseException(403, "Account is disabled");
        }

        String role = user.getIdentity() == 1 ? "TEACHER" : "STUDENT";
        String token = jwtUtils.createToken(user.getId(), role, new HashMap<>());

        String redisKey = "user:" + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, jwtUtils.getExpiration() + 300000, TimeUnit.MILLISECONDS);

        return new LoginResponse(token, user.getId(), role);
    }

    @Override
    public void register(RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new BaseException(400, "Username and password are required");
        }

        // Check if username exists
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BaseException(400, "Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // Use BCrypt to hash password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIdentity(request.getIdentity() != null ? request.getIdentity() : 0);
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // Enabled by default

        userMapper.insert(user);
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete("user:" + userId);
    }
}
