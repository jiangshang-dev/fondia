-- ============================================================
-- fd_user 会员快照字段（MySQL 5.7 / 不支持 ADD COLUMN IF NOT EXISTS 时使用）
-- 若 init_member_tables.sql 中 ALTER 已成功，本脚本可跳过
-- ============================================================

-- 执行前请先检查列是否已存在，避免重复报错：
-- SHOW COLUMNS FROM fd_user LIKE 'member_status';

ALTER TABLE `fd_user`
    ADD COLUMN `member_status` tinyint NOT NULL DEFAULT 0 COMMENT '会员状态：0非会员 1有效 2已过期' AFTER `check_in_days`;

ALTER TABLE `fd_user`
    ADD COLUMN `member_expire_time` bigint DEFAULT NULL COMMENT '会员到期时间(毫秒时间戳，冗余快照)' AFTER `member_status`;

ALTER TABLE `fd_user`
    ADD COLUMN `member_plan_code` varchar(50) DEFAULT NULL COMMENT '当前生效套餐编码(冗余快照)' AFTER `member_expire_time`;
