-- ============================================================
-- AI 专属方案（营养咨询）每日次数配额
-- 依赖：init_member_tables.sql 已执行
-- ============================================================

-- 1. 会员套餐增加「AI营养咨询每日次数」字段（0=不限，NULL=沿用免费额度）
-- 若已存在该列请跳过本句
ALTER TABLE `fd_member_plan`
    ADD COLUMN `ai_nutrition_daily_limit` int DEFAULT NULL COMMENT 'AI营养咨询每日次数，0=不限，NULL=非会员免费额度';

-- 2. 全局免费配额配置（非会员）
CREATE TABLE IF NOT EXISTS `fd_ai_quota_config` (
    `id`               bigint       NOT NULL COMMENT '主键',
    `feature_code`     varchar(50)  NOT NULL COMMENT '功能编码，如 AI_NUTRITION',
    `feature_name`     varchar(100) NOT NULL COMMENT '功能名称',
    `free_daily_limit` int          NOT NULL DEFAULT 2 COMMENT '非会员每日免费次数',
    `status`           tinyint      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `remark`           varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by`        varchar(50)  DEFAULT NULL,
    `create_time`      datetime     DEFAULT NULL,
    `update_by`        varchar(50)  DEFAULT NULL,
    `update_time`      datetime     DEFAULT NULL,
    `sys_org_code`     varchar(64)  DEFAULT NULL COMMENT '所属部门',
    `del_flag`         tinyint      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_feature_code` (`feature_code`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI功能配额全局配置';

-- 3. 用户每日使用计数
CREATE TABLE IF NOT EXISTS `fd_ai_quota_usage` (
    `id`           bigint      NOT NULL COMMENT '主键',
    `user_id`      bigint      NOT NULL COMMENT '用户ID',
    `feature_code` varchar(50) NOT NULL COMMENT '功能编码',
    `usage_date`   int         NOT NULL COMMENT '使用日期 yyyyMMdd',
    `used_count`   int         NOT NULL DEFAULT 0 COMMENT '已使用次数',
    `create_time`  datetime    DEFAULT NULL,
    `update_time`  datetime    DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_feature_date` (`user_id`, `feature_code`, `usage_date`),
    KEY `idx_usage_date` (`usage_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI功能每日使用计数';

-- 4. 初始配置：非会员每日 2 次
INSERT INTO `fd_ai_quota_config` (
    `id`, `feature_code`, `feature_name`, `free_daily_limit`, `status`, `remark`,
    `create_by`, `create_time`, `del_flag`
) VALUES (
    3005000000000000001, 'AI_NUTRITION', 'AI营养专属方案', 2, 1, '非会员每日免费生成次数，会员按套餐配置',
    'admin', NOW(), 0
) ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 5. 各会员套餐默认次数（可在管理后台调整）
UPDATE `fd_member_plan` SET `ai_nutrition_daily_limit` = 10 WHERE `plan_code` = 'VIP_MONTH' AND (`ai_nutrition_daily_limit` IS NULL);
UPDATE `fd_member_plan` SET `ai_nutrition_daily_limit` = 10 WHERE `plan_code` = 'VIP_MONTH_IOS' AND (`ai_nutrition_daily_limit` IS NULL);
UPDATE `fd_member_plan` SET `ai_nutrition_daily_limit` = 10 WHERE `plan_code` = 'VIP_MONTH_INTL' AND (`ai_nutrition_daily_limit` IS NULL);
UPDATE `fd_member_plan` SET `ai_nutrition_daily_limit` = 20 WHERE `plan_code` = 'VIP_QUARTER' AND (`ai_nutrition_daily_limit` IS NULL);
UPDATE `fd_member_plan` SET `ai_nutrition_daily_limit` = 50 WHERE `plan_code` = 'VIP_YEAR' AND (`ai_nutrition_daily_limit` IS NULL);

-- 6. 终身会员套餐（可选，未存在则插入）
INSERT INTO `fd_member_plan` (
    `id`, `plan_code`, `name`, `subtitle`, `duration_days`,
    `price`, `original_price`, `currency`, `platform_scope`,
    `benefits_json`, `bonus_points`, `bonus_growth`, `ai_nutrition_daily_limit`, `sort_no`, `status`,
    `create_by`, `create_time`, `del_flag`
) SELECT
    3001000000000000006, 'VIP_LIFETIME', '终身会员', '一次开通，永久尊享', 36500,
    598.00, 998.00, 'CNY', 'ALL',
    '[{"icon":"recipe","title":"专属食谱","desc":"解锁全部高级内容"},{"icon":"ai","title":"AI营养师","desc":"无限次专属方案"}]',
    2000, 1000, 0, 6, 1, 'admin', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_member_plan` WHERE `plan_code` = 'VIP_LIFETIME' AND `del_flag` = 0);
