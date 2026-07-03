-- ============================================================
-- Fondia 积分商城/地址/虚拟履约 增量脚本（在 init_member_tables 之后执行）
-- ============================================================

-- 1. 用户收货地址
CREATE TABLE IF NOT EXISTS `fd_user_address` (
    `id`              bigint       NOT NULL COMMENT '主键',
    `user_id`         bigint       NOT NULL COMMENT '用户ID',
    `receiver_name`   varchar(100) NOT NULL COMMENT '收货人',
    `receiver_phone`  varchar(30)  NOT NULL COMMENT '手机号',
    `province`        varchar(50)  DEFAULT NULL COMMENT '省',
    `city`            varchar(50)  DEFAULT NULL COMMENT '市',
    `district`        varchar(50)  DEFAULT NULL COMMENT '区',
    `detail_address`  varchar(500) NOT NULL COMMENT '详细地址',
    `is_default`      tinyint      NOT NULL DEFAULT 0 COMMENT '是否默认：0否 1是',
    `create_time`     bigint       DEFAULT NULL COMMENT '创建时间(毫秒)',
    `update_time`     bigint       DEFAULT NULL COMMENT '更新时间(毫秒)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址';

-- 2. 商城商品增加虚拟履约配置（实物无需此字段；若已存在列请跳过）
ALTER TABLE `fd_mall_product`
    ADD COLUMN `virtual_config_json` text COMMENT '虚拟商品履约JSON，如 {"fulfillType":"MEMBER_DAYS","memberDays":3}';

-- 3. 永久会员套餐（若不存在则插入）
INSERT INTO `fd_member_plan` (
    `id`, `plan_code`, `name`, `subtitle`, `duration_days`,
    `price`, `original_price`, `currency`, `platform_scope`,
    `benefits_json`, `bonus_points`, `bonus_growth`, `sort_no`, `status`,
    `create_by`, `create_time`, `del_flag`
)
SELECT
    3001000000000000006, 'VIP_FOREVER', '永久会员', '一次开通终身尊享', 36500,
    598.00, 998.00, 'CNY', 'ALL',
    '[{"icon":"crown","title":"终身特权","desc":"永久享受会员权益"}]',
    2000, 1000, 6, 1, 'admin', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_member_plan` WHERE `plan_code` = 'VIP_FOREVER' AND `del_flag` = 0);

-- 4. 更新虚拟商品履约配置示例
UPDATE `fd_mall_product`
SET `virtual_config_json` = '{"fulfillType":"MEMBER_DAYS","memberDays":3,"planCode":"VIP_TRIAL"}',
    `product_type` = 'VIRTUAL'
WHERE `product_code` = 'VIP_TRIAL_3D' AND (`virtual_config_json` IS NULL OR `virtual_config_json` = '');

UPDATE `fd_mall_product`
SET `virtual_config_json` = '{"fulfillType":"COUPON","couponAmount":5}',
    `product_type` = 'VIRTUAL'
WHERE `product_code` = 'COUPON_5' AND (`virtual_config_json` IS NULL OR `virtual_config_json` = '');

UPDATE `fd_mall_product`
SET `virtual_config_json` = '{"fulfillType":"COUPON","couponAmount":10}',
    `product_type` = 'VIRTUAL'
WHERE `product_code` = 'COUPON_10' AND (`virtual_config_json` IS NULL OR `virtual_config_json` = '');
