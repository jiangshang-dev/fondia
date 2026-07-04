-- 会员套餐增加 IAP 商品 ID（Apple / Google）
ALTER TABLE `fd_member_plan`
    ADD COLUMN `apple_product_id` varchar(128) DEFAULT NULL COMMENT 'Apple IAP 商品ID' AFTER `status`;

ALTER TABLE `fd_member_plan`
    ADD COLUMN `google_product_id` varchar(128) DEFAULT NULL COMMENT 'Google Play 商品ID' AFTER `apple_product_id`;
