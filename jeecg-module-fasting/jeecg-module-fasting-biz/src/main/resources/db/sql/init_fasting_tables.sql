-- Fast16 轻断食表结构初始化

CREATE TABLE IF NOT EXISTS `fd_fast_plan` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '计划编码 16_8/14_10/18_6/20_4',
  `name` varchar(64) NOT NULL COMMENT '展示名',
  `fast_hours` int NOT NULL COMMENT '断食小时数',
  `eat_hours` int NOT NULL COMMENT '进食小时数',
  `description` varchar(255) DEFAULT NULL COMMENT '说明',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='断食计划模板';

CREATE TABLE IF NOT EXISTS `fd_fast_user_setting` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `device_id` varchar(64) DEFAULT NULL COMMENT '设备标识',
  `plan_code` varchar(32) NOT NULL DEFAULT '16_8' COMMENT '当前计划',
  `eat_start_time` time DEFAULT '12:00:00' COMMENT '进食开始时间',
  `haptic_enabled` tinyint DEFAULT 1 COMMENT '触感反馈',
  `locale` varchar(16) DEFAULT 'zh_CN' COMMENT '语言',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户断食设置';

CREATE TABLE IF NOT EXISTS `fd_fast_session` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `device_id` varchar(64) DEFAULT NULL COMMENT '设备标识',
  `plan_code` varchar(32) DEFAULT NULL COMMENT '计划编码',
  `phase` varchar(16) NOT NULL COMMENT 'FASTING/EATING',
  `start_time` datetime NOT NULL COMMENT '阶段开始',
  `end_time` datetime DEFAULT NULL COMMENT '阶段结束',
  `duration_sec` int DEFAULT NULL COMMENT '实际时长秒',
  `client_uuid` varchar(64) DEFAULT NULL COMMENT '客户端幂等键',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_uuid` (`client_uuid`),
  KEY `idx_user_device` (`user_id`, `device_id`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='断食进食会话';

CREATE TABLE IF NOT EXISTS `fd_fast_daily_stat` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `device_id` varchar(64) DEFAULT NULL COMMENT '设备标识',
  `plan_code` varchar(32) DEFAULT NULL COMMENT '计划编码',
  `target_fast_sec` int DEFAULT 0 COMMENT '目标断食秒数',
  `actual_fast_sec` int DEFAULT 0 COMMENT '实际断食秒数',
  `completed` tinyint DEFAULT 0 COMMENT '是否达标',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_user_date` (`user_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='断食日统计';

CREATE TABLE IF NOT EXISTS `fd_fast_ai_message` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT '对话会话',
  `role` varchar(16) NOT NULL COMMENT 'user/assistant',
  `content` text COMMENT '内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_session` (`user_id`, `session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录';

-- 预置计划
INSERT INTO `fd_fast_plan` (`id`, `code`, `name`, `fast_hours`, `eat_hours`, `description`, `sort_order`, `status`) VALUES
(1, '14_10', '14:10 轻断食', 14, 10, '适合入门，断食14小时', 1, 1),
(2, '16_8', '16:8 轻断食', 16, 8, '经典方案，断食16小时', 2, 1),
(3, '18_6', '18:6 轻断食', 18, 6, '进阶方案，断食18小时', 3, 1),
(4, '20_4', '20:4 轻断食', 20, 4, '战士饮食，断食20小时', 4, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
