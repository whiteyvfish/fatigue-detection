package com.fatigue.detection.controller;

import com.fatigue.detection.dto.ApiResponse;
import com.fatigue.detection.dto.LoginRequest;
import com.fatigue.detection.dto.LoginResponse;
import com.fatigue.detection.dto.RegisterRequest;
import com.fatigue.detection.entity.User;
import com.fatigue.detection.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        try {
            LoginResponse resp = authService.register(req);
            return ApiResponse.success(resp, "注册成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            LoginResponse resp = authService.login(req);
            return ApiResponse.success(resp, "登录成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前用户信息（通过JWT Token）
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(401, "未登录");
        }

        String token = authHeader.substring(7);
        User user = authService.getCurrentUser(token);

        if (user == null) {
            return ApiResponse.error(401, "Token无效或已过期");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("role", user.getRole());
        data.put("avatar", user.getAvatar());
        data.put("lastLoginTime", user.getLastLoginTime());

        return ApiResponse.success(data);
    }
}
