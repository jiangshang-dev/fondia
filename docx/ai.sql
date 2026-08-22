-- Fondia AI 对话历史（MySQL 8+）
CREATE TABLE IF NOT EXISTS `fd_ai_session` (
  `id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `source` varchar(32) NOT NULL DEFAULT 'ai_tab' COMMENT '来源：ai_tab等',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/PROCESSING/STOPPED/COMPLETED',
  `title` varchar(200) DEFAULT NULL COMMENT '首条用户消息摘要',
  `last_message` varchar(500) DEFAULT NULL COMMENT '列表展示摘要',
  `last_message_time` bigint DEFAULT NULL COMMENT '最后消息时间（毫秒）',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_fd_ai_session_user_source_time` (`user_id`,`source`,`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

CREATE TABLE IF NOT EXISTS `fd_ai_message` (
  `id` bigint NOT NULL COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `sender_type` varchar(16) NOT NULL COMMENT 'USER/AI',
  `msg_type` varchar(16) NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT',
  `content` longtext NOT NULL COMMENT '消息正文',
  `status` varchar(16) NOT NULL DEFAULT 'COMPLETED' COMMENT 'STREAMING/COMPLETED/STOPPED/ERROR',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_fd_ai_message_session_time` (`session_id`,`create_time`),
  KEY `idx_fd_ai_message_user_session` (`user_id`,`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';
