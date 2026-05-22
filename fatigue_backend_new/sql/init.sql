-- ============================================
-- 疲劳检测系统 - MySQL 建表脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS fatigue DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fatigue;

-- 系统用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    username        VARCHAR(64)     NOT NULL                COMMENT '用户名（登录账号）',
    password        VARCHAR(256)    NOT NULL                COMMENT '密码（BCrypt加密）',
    nickname        VARCHAR(64)     DEFAULT NULL            COMMENT '昵称',
    email           VARCHAR(128)    DEFAULT NULL            COMMENT '邮箱',
    phone           VARCHAR(20)     DEFAULT NULL            COMMENT '手机号',
    avatar          VARCHAR(256)    DEFAULT NULL            COMMENT '头像URL',
    role            VARCHAR(20)     NOT NULL DEFAULT 'user' COMMENT '角色: user/admin',
    status          TINYINT         NOT NULL DEFAULT 1      COMMENT '状态: 1=启用, 0=禁用',
    deleted         TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除: 1=已删除, 0=正常',
    last_login_time DATETIME        DEFAULT NULL            COMMENT '最后登录时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_deleted (deleted),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 插入默认管理员（密码: admin123，BCrypt加密）
INSERT INTO sys_user (username, password, nickname, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'admin', 1);
-- 注意：上面的BCrypt占位值需替换为实际加密值
-- 实际使用前，先启动后端，调用注册接口，或使用在线BCrypt工具生成
