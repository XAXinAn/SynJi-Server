CREATE TABLE IF NOT EXISTS `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone_number` VARCHAR(20) NOT NULL COMMENT '手机号',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone_number` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `verification_codes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone_number` VARCHAR(20) NOT NULL COMMENT '手机号',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `is_used` TINYINT(1) DEFAULT 0 COMMENT '是否已使用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone_code` (`phone_number`, `code`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码记录表';

CREATE TABLE IF NOT EXISTS `schedules` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日程ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '日程标题',
  `date` DATE NOT NULL COMMENT '日期 (yyyy-MM-dd)',
  `time` TIME NOT NULL DEFAULT '00:00:00' COMMENT '时间 (HH:mm:ss)',
  `is_all_day` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否全天',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '地点',
  `belonging` VARCHAR(128) NOT NULL DEFAULT '个人' COMMENT '所属分类: 个人 或 群组名称',
  `is_important` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否重要',
  `notes` TEXT DEFAULT NULL COMMENT '备注',
  `is_ai_generated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否由AI自动提取生成',
  `is_viewed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '用户是否已查看',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程表';

CREATE TABLE IF NOT EXISTS `groups` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` VARCHAR(128) NOT NULL COMMENT '群组名称',
  `invite_code` VARCHAR(10) NOT NULL COMMENT '邀请码',
  `owner_id` BIGINT NOT NULL COMMENT '群主ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

CREATE TABLE IF NOT EXISTS `group_members` (
  `group_id` BIGINT NOT NULL COMMENT '群组ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: OWNER, ADMIN, MEMBER',
  PRIMARY KEY (`group_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';
