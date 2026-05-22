package com.fatigue.detection.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private String avatar;

    public LoginResponse(String token, Long userId, String username, String nickname, String role, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.avatar = avatar;
    }
}
