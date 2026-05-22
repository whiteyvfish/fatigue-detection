package com.fatigue.detection.service;

import com.fatigue.detection.dto.LoginRequest;
import com.fatigue.detection.dto.LoginResponse;
import com.fatigue.detection.dto.RegisterRequest;
import com.fatigue.detection.entity.User;
import com.fatigue.detection.repository.UserRepository;
import com.fatigue.detection.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsernameAndDeleted(req.getUsername(), 0)) {
            throw new RuntimeException("用户名已存在");
        }
        if (req.getEmail() != null && !req.getEmail().isEmpty()
                && userRepository.existsByEmailAndDeleted(req.getEmail(), 0)) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("user");
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户注册成功: {}", user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getRole(), user.getAvatar());
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest req) {
        Optional<User> userOpt = userRepository.findByUsernameAndDeleted(req.getUsername(), 0);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户名或密码错误");
        }

        User user = userOpt.get();
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getRole(), user.getAvatar());
    }

    /**
     * 通过 JWT Token 获取当前用户
     */
    public User getCurrentUser(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            return null;
        }
        Long userId = jwtUtil.getUserId(token);
        return userRepository.findById(userId).orElse(null);
    }
}
