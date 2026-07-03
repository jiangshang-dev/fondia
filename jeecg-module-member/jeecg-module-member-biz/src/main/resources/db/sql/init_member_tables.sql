-- ============================================================
-- Fondia 会员 / 积分 / 签到 / 积分商城 模块表结构（MySQL 8）
-- 执行顺序：先本脚本，再 init_member_seed_data.sql
-- 说明：fd_user.points / check_in_days 保留为冗余快照，权威数据以流水与订阅表为准
-- ============================================================

-- ------------------------------------------------------------
-- 0. fd_user 扩展字段（会员状态快照，便于 profile 一次查询）
-- 请单独执行：db/alter_fd_user_member.sql
-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- 1. 会员套餐表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_member_plan` (
    `id`               bigint         NOT NULL COMMENT '主键',
    `plan_code`        varchar(50)    NOT NULL COMMENT '套餐编码，如 VIP_MONTH / VIP_YEAR',
    `name`             varchar(100)   NOT NULL COMMENT '套餐名称',
    `subtitle`         varchar(200)   DEFAULT NULL COMMENT '副标题/卖点',
    `duration_days`    int            NOT NULL COMMENT '有效天数',
    `price`            decimal(10, 2) NOT NULL COMMENT '售价',
    `original_price`   decimal(10, 2) DEFAULT NULL COMMENT '原价(划线价)',
    `currency`         varchar(10)    NOT NULL DEFAULT 'CNY' COMMENT '币种：CNY/USD 等',
    `platform_scope`   varchar(30)    NOT NULL DEFAULT 'ALL' COMMENT '适用平台：ALL/IOS/ANDROID/WEB',
    `benefits_json`    text           COMMENT '权益说明JSON',
    `bonus_points`     int            NOT NULL DEFAULT 0 COMMENT '开通赠送积分',
    `bonus_growth`     int            NOT NULL DEFAULT 0 COMMENT '开通赠送成长值',
    `sort_no`          int            NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`           tinyint        NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    `create_by`        varchar(50)    DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime       DEFAULT NULL COMMENT '创建时间',
    `update_by`        varchar(50)    DEFAULT NULL COMMENT '更新人',
    `update_time`      datetime       DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`     varchar(64)    DEFAULT NULL COMMENT '所属部门',
    `del_flag`         tinyint        NOT NULL DEFAULT 0 COMMENT '删除：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plan_code` (`plan_code`, `del_flag`),
    KEY `idx_status_sort` (`status`, `sort_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员套餐表';

-- ------------------------------------------------------------
-- 2. 会员订阅表（用户当前/历史订阅）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_member_subscription` (
    `id`                  bigint       NOT NULL COMMENT '主键',
    `user_id`             bigint       NOT NULL COMMENT '用户ID(fd_user.id)',
    `plan_id`             bigint       NOT NULL COMMENT '套餐ID(fd_member_plan.id)',
    `plan_code`           varchar(50)  NOT NULL COMMENT '套餐编码(冗余)',
    `status`              tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0待生效 1生效中 2已过期 3已取消',
    `start_time`          bigint       NOT NULL COMMENT '生效开始(毫秒)',
    `expire_time`         bigint       NOT NULL COMMENT '到期时间(毫秒)',
    `auto_renew`          tinyint      NOT NULL DEFAULT 0 COMMENT '自动续费：0否 1是',
    `source_channel`      varchar(30)  DEFAULT NULL COMMENT '开通渠道：ALIPAY/APPLE_IAP/GOOGLE_PLAY/ADMIN',
    `external_sub_id`     varchar(200) DEFAULT NULL COMMENT '第三方订阅ID(苹果/谷歌)',
    `last_order_id`       bigint       DEFAULT NULL COMMENT '最近关联订单ID',
    `remark`              varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time`         bigint       DEFAULT NULL COMMENT '创建时间(毫秒)',
    `update_time`         bigint       DEFAULT NULL COMMENT '更新时间(毫秒)',
    PRIMARY KEY (`id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_user_expire` (`user_id`, `expire_time`),
    KEY `idx_external_sub` (`source_channel`, `external_sub_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员订阅表';

-- ------------------------------------------------------------
-- 3. 会员订单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_member_order` (
    `id`                  bigint         NOT NULL COMMENT '主键',
    `order_no`            varchar(64)    NOT NULL COMMENT '业务订单号',
    `user_id`             bigint         NOT NULL COMMENT '用户ID',
    `plan_id`             bigint         NOT NULL COMMENT '套餐ID',
    `plan_code`           varchar(50)    NOT NULL COMMENT '套餐编码(冗余)',
    `plan_name`           varchar(100)   NOT NULL COMMENT '套餐名称(冗余)',
    `duration_days`       int            NOT NULL COMMENT '购买天数(冗余)',
    `amount`              decimal(10, 2) NOT NULL COMMENT '应付金额',
    `currency`            varchar(10)    NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `pay_channel`         varchar(30)    DEFAULT NULL COMMENT '支付渠道：ALIPAY/APPLE_IAP/GOOGLE_PLAY',
    `pay_status`            tinyint        NOT NULL DEFAULT 0 COMMENT '支付状态：0待支付 1已支付 2失败 3已关闭 4已退款',
    `client_platform`     varchar(20)    DEFAULT NULL COMMENT '客户端：ios/android/web',
    `client_region`       varchar(20)    DEFAULT NULL COMMENT '客户端地区：CN/US 等',
    `client_ip`           varchar(64)    DEFAULT NULL COMMENT '下单IP',
    `subscription_id`     bigint         DEFAULT NULL COMMENT '支付成功后关联订阅ID',
    `paid_time`           bigint         DEFAULT NULL COMMENT '支付成功时间(毫秒)',
    `close_time`          bigint         DEFAULT NULL COMMENT '关单时间(毫秒)',
    `remark`              varchar(500)   DEFAULT NULL COMMENT '备注',
    `create_time`         bigint         DEFAULT NULL COMMENT '创建时间(毫秒)',
    `update_time`         bigint         DEFAULT NULL COMMENT '更新时间(毫秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `pay_status`),
    KEY `idx_pay_channel` (`pay_channel`, `pay_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员订单表';

-- ------------------------------------------------------------
-- 4. 支付流水表（多渠道统一）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_payment_record` (
    `id`                  bigint         NOT NULL COMMENT '主键',
    `payment_no`          varchar(64)    NOT NULL COMMENT '支付流水号',
    `order_no`            varchar(64)    NOT NULL COMMENT '关联业务订单号',
    `order_type`          varchar(30)    NOT NULL COMMENT '订单类型：MEMBER/MALL',
    `user_id`             bigint         NOT NULL COMMENT '用户ID',
    `pay_channel`         varchar(30)    NOT NULL COMMENT 'ALIPAY/APPLE_IAP/GOOGLE_PLAY',
    `amount`              decimal(10, 2) NOT NULL COMMENT '支付金额',
    `currency`            varchar(10)    NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `pay_status`          tinyint        NOT NULL DEFAULT 0 COMMENT '0待支付 1成功 2失败 3退款',
    `external_trade_no`   varchar(128)   DEFAULT NULL COMMENT '第三方交易号',
    `external_payload`    text           COMMENT '第三方回调原文/验单报文',
    `notify_time`         bigint         DEFAULT NULL COMMENT '回调时间(毫秒)',
    `create_time`         bigint         DEFAULT NULL COMMENT '创建时间(毫秒)',
    `update_time`         bigint         DEFAULT NULL COMMENT '更新时间(毫秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_channel_trade` (`pay_channel`, `external_trade_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付流水表';

-- ------------------------------------------------------------
-- 5. 积分账户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_points_account` (
    `id`            bigint NOT NULL COMMENT '主键',
    `user_id`       bigint NOT NULL COMMENT '用户ID',
    `balance`       int    NOT NULL DEFAULT 0 COMMENT '可用积分',
    `frozen`        int    NOT NULL DEFAULT 0 COMMENT '冻结积分',
    `total_earned`  int    NOT NULL DEFAULT 0 COMMENT '累计获得',
    `total_spent`   int    NOT NULL DEFAULT 0 COMMENT '累计消耗',
    `version`       int    NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`   bigint DEFAULT NULL COMMENT '创建时间(毫秒)',
    `update_time`   bigint DEFAULT NULL COMMENT '更新时间(毫秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分账户表';

-- ------------------------------------------------------------
-- 6. 积分流水表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_points_ledger` (
    `id`              bigint       NOT NULL COMMENT '主键',
    `user_id`         bigint       NOT NULL COMMENT '用户ID',
    `change_amount`   int          NOT NULL COMMENT '变动积分(正增负减)',
    `balance_after`   int          NOT NULL COMMENT '变动后余额',
    `biz_type`        varchar(30)  NOT NULL COMMENT '业务类型：CHECKIN/TASK/MALL_REDEEM/MEMBER_GIFT/ADMIN_ADJUST/REFUND',
    `biz_id`          varchar(64)  DEFAULT NULL COMMENT '业务关联ID',
    `title`           varchar(200) DEFAULT NULL COMMENT '展示标题',
    `remark`          varchar(500) DEFAULT NULL COMMENT '备注',
    `operator_id`     bigint       DEFAULT NULL COMMENT '操作人(运营调账)',
    `create_time`     bigint       NOT NULL COMMENT '创建时间(毫秒)',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分流水表';

-- ------------------------------------------------------------
-- 7. 积分任务表（去赚积分）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_points_task` (
    `id`              bigint       NOT NULL COMMENT '主键',
    `task_code`       varchar(50)  NOT NULL COMMENT '任务编码，如 BROWSE_RECIPE / POST_COMMENT',
    `title`           varchar(100) NOT NULL COMMENT '任务标题',
    `description`     varchar(500) DEFAULT NULL COMMENT '任务描述',
    `reward_points`   int          NOT NULL DEFAULT 0 COMMENT '奖励积分',
    `action_type`     varchar(30)  NOT NULL COMMENT '触发类型：MANUAL/BROWSE/COMMENT/SHARE/PROFILE',
    `daily_limit`     int          NOT NULL DEFAULT 1 COMMENT '每日可完成次数，0不限',
    `total_limit`     int          NOT NULL DEFAULT 0 COMMENT '总次数上限，0不限',
    `icon`            varchar(200) DEFAULT NULL COMMENT '图标',
    `jump_url`        varchar(500) DEFAULT NULL COMMENT 'App内跳转',
    `sort_no`         int          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`          tinyint      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    `create_by`       varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime     DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`    varchar(64)  DEFAULT NULL COMMENT '所属部门',
    `del_flag`        tinyint      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_code` (`task_code`, `del_flag`),
    KEY `idx_status_sort` (`status`, `sort_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分任务表';

-- ------------------------------------------------------------
-- 8. 积分任务完成记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_points_task_record` (
    `id`              bigint      NOT NULL COMMENT '主键',
    `user_id`         bigint      NOT NULL COMMENT '用户ID',
    `task_id`         bigint      NOT NULL COMMENT '任务ID',
    `task_code`       varchar(50) NOT NULL COMMENT '任务编码(冗余)',
    `complete_date`   date        NOT NULL COMMENT '完成日期(用于日限统计)',
    `reward_points`   int         NOT NULL DEFAULT 0 COMMENT '本次奖励积分',
    `biz_ref`         varchar(64) DEFAULT NULL COMMENT '业务引用(如 recipeId/commentId)',
    `create_time`     bigint      NOT NULL COMMENT '完成时间(毫秒)',
    PRIMARY KEY (`id`),
    KEY `idx_user_task_date` (`user_id`, `task_id`, `complete_date`),
    KEY `idx_user_date` (`user_id`, `complete_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分任务完成记录';

-- ------------------------------------------------------------
-- 9. 签到奖励配置（7日循环）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_checkin_config` (
    `id`              bigint      NOT NULL COMMENT '主键',
    `day_index`       tinyint     NOT NULL COMMENT '第几天(1-7循环)',
    `reward_points`   int         NOT NULL DEFAULT 0 COMMENT '奖励积分',
    `reward_growth`   int         NOT NULL DEFAULT 0 COMMENT '奖励成长值',
    `status`          tinyint     NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    `create_by`       varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime    DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime    DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`    varchar(64) DEFAULT NULL COMMENT '所属部门',
    `del_flag`        tinyint     NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_day_index` (`day_index`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='签到奖励配置';

-- ------------------------------------------------------------
-- 10. 用户签到记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_checkin_record` (
    `id`              bigint   NOT NULL COMMENT '主键',
    `user_id`         bigint   NOT NULL COMMENT '用户ID',
    `checkin_date`    date     NOT NULL COMMENT '签到日期',
    `streak_days`     int      NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    `cycle_day_index` tinyint  NOT NULL DEFAULT 1 COMMENT '周期内第几天(1-7)',
    `reward_points`   int      NOT NULL DEFAULT 0 COMMENT '获得积分',
    `reward_growth`   int      NOT NULL DEFAULT 0 COMMENT '获得成长值',
    `create_time`     bigint   NOT NULL COMMENT '签到时间(毫秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户签到记录';

-- ------------------------------------------------------------
-- 11. 积分商城商品
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_mall_product` (
    `id`                bigint       NOT NULL COMMENT '主键',
    `product_code`      varchar(50)  NOT NULL COMMENT '商品编码',
    `name`              varchar(200) NOT NULL COMMENT '商品名称',
    `subtitle`          varchar(500) DEFAULT NULL COMMENT '副标题',
    `cover_image`       varchar(500) DEFAULT NULL COMMENT '封面图',
    `product_type`      varchar(20)  NOT NULL COMMENT '类型：VIRTUAL/PHYSICAL/COUPON',
    `points_price`      int          NOT NULL COMMENT '兑换所需积分',
    `market_price`      decimal(10, 2) DEFAULT NULL COMMENT '参考市场价',
    `stock`             int          NOT NULL DEFAULT 0 COMMENT '库存，-1不限',
    `sold_count`        int          NOT NULL DEFAULT 0 COMMENT '已兑换数量',
    `limit_per_user`    int          NOT NULL DEFAULT 0 COMMENT '每用户限兑，0不限',
    `detail_html`       text         COMMENT '详情富文本',
    `sort_no`           int          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`            tinyint      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    `create_by`         varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime     DEFAULT NULL COMMENT '创建时间',
    `update_by`         varchar(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`       datetime     DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`      varchar(64)  DEFAULT NULL COMMENT '所属部门',
    `del_flag`          tinyint      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`, `del_flag`),
    KEY `idx_status_sort` (`status`, `sort_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分商城商品';

-- ------------------------------------------------------------
-- 12. 积分商城兑换订单
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_mall_order` (
    `id`                bigint       NOT NULL COMMENT '主键',
    `order_no`          varchar(64)  NOT NULL COMMENT '兑换订单号',
    `user_id`           bigint       NOT NULL COMMENT '用户ID',
    `product_id`        bigint       NOT NULL COMMENT '商品ID',
    `product_code`      varchar(50)  NOT NULL COMMENT '商品编码(冗余)',
    `product_name`      varchar(200) NOT NULL COMMENT '商品名称(冗余)',
    `product_type`      varchar(20)  NOT NULL COMMENT '商品类型(冗余)',
    `points_cost`       int          NOT NULL COMMENT '消耗积分',
    `quantity`          int          NOT NULL DEFAULT 1 COMMENT '数量',
    `order_status`      tinyint      NOT NULL DEFAULT 0 COMMENT '0待处理 1已发货 2已完成 3已取消',
    `receiver_name`     varchar(100) DEFAULT NULL COMMENT '收货人',
    `receiver_phone`    varchar(30)  DEFAULT NULL COMMENT '收货电话',
    `receiver_address`  varchar(500) DEFAULT NULL COMMENT '收货地址',
    `logistics_no`      varchar(100) DEFAULT NULL COMMENT '物流单号',
    `virtual_payload`   text         COMMENT '虚拟商品发放内容(JSON)',
    `remark`            varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time`       bigint       DEFAULT NULL COMMENT '创建时间(毫秒)',
    `update_time`       bigint       DEFAULT NULL COMMENT '更新时间(毫秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `order_status`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分商城兑换订单';
