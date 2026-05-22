package com.fatigue.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
@TableName("sys_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(length = 64)
    private String nickname;

    @Column(nullable = false, length = 256)
    @JsonIgnore
    private String password;

    @Column(length = 128)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 256)
    private String avatar;

    @Column(length = 20)
    private String role;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime lastLoginTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) role = "user";
        if (status == null) status = 1;
        if (deleted == null) deleted = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
