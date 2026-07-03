-- ============================================================
-- Fondia 会员 / 积分 / 签到 初始数据
-- 依赖：init_member_tables.sql 已执行
-- ID 使用固定雪花风格占位，生产可按项目 ID 生成策略调整
-- ============================================================

-- ------------------------------------------------------------
-- 1. 会员套餐（月/季/年）
-- ------------------------------------------------------------
INSERT INTO `fd_member_plan` (
    `id`, `plan_code`, `name`, `subtitle`, `duration_days`,
    `price`, `original_price`, `currency`, `platform_scope`,
    `benefits_json`, `bonus_points`, `bonus_growth`, `sort_no`, `status`,
    `create_by`, `create_time`, `del_flag`
) VALUES
(
    3001000000000000001, 'VIP_MONTH', '月度会员', '灵活体验，随时可续', 30,
    18.00, 25.00, 'CNY', 'ALL',
    '[{"icon":"coupon","title":"每月礼券","desc":"无门槛优惠券"},{"icon":"recipe","title":"专属食谱","desc":"解锁高级料理"},{"icon":"growth","title":"加速升级","desc":"1.5倍成长值"}]',
    100, 50, 1, 1, 'admin', NOW(), 0
),
(
    3001000000000000002, 'VIP_QUARTER', '季度会员', '高性价比之选', 90,
    48.00, 68.00, 'CNY', 'ALL',
    '[{"icon":"coupon","title":"每月礼券","desc":"无门槛优惠券"},{"icon":"recipe","title":"专属食谱","desc":"解锁高级料理"},{"icon":"service","title":"专属客服","desc":"优先响应"}]',
    300, 150, 2, 1, 'admin', NOW(), 0
),
(
    3001000000000000003, 'VIP_YEAR', '年度会员', '全年畅享全部特权', 365,
    168.00, 298.00, 'CNY', 'ALL',
    '[{"icon":"coupon","title":"每月礼券","desc":"无门槛优惠券"},{"icon":"recipe","title":"专属食谱","desc":"解锁高级料理"},{"icon":"ad","title":"免除广告","desc":"纯净体验"},{"icon":"gift","title":"生日好礼","desc":"神秘大礼包"}]',
    1000, 500, 3, 1, 'admin', NOW(), 0
),
(
    3001000000000000004, 'VIP_MONTH_IOS', '月度会员(iOS)', 'App Store 订阅', 30,
    18.00, 25.00, 'CNY', 'IOS',
    '[{"icon":"coupon","title":"每月礼券","desc":"无门槛优惠券"},{"icon":"recipe","title":"专属食谱","desc":"解锁高级料理"}]',
    100, 50, 4, 1, 'admin', NOW(), 0
),
(
    3001000000000000005, 'VIP_MONTH_INTL', 'Monthly VIP', 'International plan', 30,
    2.99, 4.99, 'USD', 'ANDROID',
    '[{"icon":"recipe","title":"Premium Recipes","desc":"Unlock exclusive content"}]',
    100, 50, 5, 1, 'admin', NOW(), 0
);

-- ------------------------------------------------------------
-- 2. 签到奖励（7日循环）
-- ------------------------------------------------------------
INSERT INTO `fd_checkin_config` (
    `id`, `day_index`, `reward_points`, `reward_growth`, `status`,
    `create_by`, `create_time`, `del_flag`
) VALUES
(3002000000000000001, 1, 5,  2,  1, 'admin', NOW(), 0),
(3002000000000000002, 2, 8,  3,  1, 'admin', NOW(), 0),
(3002000000000000003, 3, 10, 5,  1, 'admin', NOW(), 0),
(3002000000000000004, 4, 12, 5,  1, 'admin', NOW(), 0),
(3002000000000000005, 5, 15, 8,  1, 'admin', NOW(), 0),
(3002000000000000006, 6, 18, 8,  1, 'admin', NOW(), 0),
(3002000000000000007, 7, 30, 15, 1, 'admin', NOW(), 0);

-- ------------------------------------------------------------
-- 3. 积分任务（去赚积分）
-- ------------------------------------------------------------
INSERT INTO `fd_points_task` (
    `id`, `task_code`, `title`, `description`, `reward_points`,
    `action_type`, `daily_limit`, `total_limit`, `sort_no`, `status`,
    `create_by`, `create_time`, `del_flag`
) VALUES
(
    3003000000000000001, 'DAILY_BROWSE', '浏览菜谱', '浏览任意菜谱详情页', 5,
    'BROWSE', 3, 0, 1, 1, 'admin', NOW(), 0
),
(
    3003000000000000002, 'POST_COMMENT', '发布评论', '对菜谱发表有效评论', 10,
    'COMMENT', 5, 0, 2, 1, 'admin', NOW(), 0
),
(
    3003000000000000003, 'SHARE_RECIPE', '分享菜谱', '分享菜谱给好友', 20,
    'SHARE', 2, 0, 3, 1, 'admin', NOW(), 0
),
(
    3003000000000000004, 'COMPLETE_PROFILE', '完善资料', '完善个人资料信息', 50,
    'PROFILE', 1, 1, 4, 1, 'admin', NOW(), 0
),
(
    3003000000000000005, 'COLLECT_RECIPE', '收藏菜谱', '收藏喜欢的菜谱', 5,
    'MANUAL', 5, 0, 5, 1, 'admin', NOW(), 0
);

-- ------------------------------------------------------------
-- 4. 积分商城示例商品
-- ------------------------------------------------------------
INSERT INTO `fd_mall_product` (
    `id`, `product_code`, `name`, `subtitle`, `cover_image`, `product_type`,
    `points_price`, `market_price`, `stock`, `limit_per_user`, `sort_no`, `status`,
    `create_by`, `create_time`, `del_flag`
) VALUES
(
    3004000000000000001, 'COUPON_5', '5元无门槛优惠券', '全场通用', NULL, 'COUPON',
    200, 5.00, -1, 5, 1, 1, 'admin', NOW(), 0
),
(
    3004000000000000002, 'COUPON_10', '10元无门槛优惠券', '满30可用', NULL, 'COUPON',
    380, 10.00, -1, 3, 2, 1, 'admin', NOW(), 0
),
(
    3004000000000000003, 'VIP_TRIAL_3D', '3天会员体验卡', '开通即享会员特权', NULL, 'VIRTUAL',
    500, NULL, 1000, 1, 3, 1, 'admin', NOW(), 0
),
(
    3004000000000000004, 'KITCHEN_TOWEL', '小厨定制厨房毛巾', '实物礼品，包邮', NULL, 'PHYSICAL',
    800, 29.90, 100, 2, 4, 1, 'admin', NOW(), 0
);
