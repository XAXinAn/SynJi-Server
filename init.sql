-- 若存在先删除数据库
DROP DATABASE IF EXISTS synji_calendar;

-- 创建数据库
CREATE DATABASE synji_calendar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE synji_calendar;

-- 若存在先删除用户表
DROP TABLE IF EXISTS `users`;

-- 创建用户表
CREATE TABLE `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone_number` VARCHAR(20) NOT NULL COMMENT '手机号',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone_number` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 若存在先删除验证码表
DROP TABLE IF EXISTS `verification_codes`;

-- 创建验证码表
CREATE TABLE `verification_codes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone_number` VARCHAR(20) NOT NULL COMMENT '手机号',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `is_used` TINYINT(1) DEFAULT 0 COMMENT '是否已使用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone_code` (`phone_number`, `code`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码记录表';

-- 若存在先删除日程表
DROP TABLE IF EXISTS `schedules`;

-- 创建日程表
CREATE TABLE `schedules` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日程ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '日程标题',
  `date` DATE NOT NULL COMMENT '日期 (yyyy-MM-dd)',
  `time` TIME DEFAULT NULL COMMENT '时间 (HH:mm:ss)',
  `is_all_day` TINYINT(1) DEFAULT 0 COMMENT '是否全天',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '地点',
  `belonging` VARCHAR(64) DEFAULT NULL COMMENT '所属分类',
  `is_important` TINYINT(1) DEFAULT 0 COMMENT '是否重要',
  `notes` TEXT DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程表';
