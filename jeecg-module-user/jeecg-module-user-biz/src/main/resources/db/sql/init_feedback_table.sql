-- 用户意见反馈表
CREATE TABLE IF NOT EXISTS `fd_user_feedback` (
    `id`            bigint        NOT NULL COMMENT '主键',
    `user_id`       bigint        NOT NULL COMMENT '用户ID',
    `feedback_type` varchar(30)   NOT NULL COMMENT '反馈类型',
    `content`       varchar(500)  NOT NULL COMMENT '反馈内容',
    `contact`       varchar(100)  DEFAULT NULL COMMENT '联系方式',
    `images`        varchar(2000) DEFAULT NULL COMMENT '图片URL列表(JSON)',
    `status`        tinyint       NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已处理',
    `create_time`   bigint        DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户意见反馈';
