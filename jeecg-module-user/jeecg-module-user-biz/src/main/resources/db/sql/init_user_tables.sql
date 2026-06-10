-- App用户表
CREATE TABLE IF NOT EXISTS `fd_user` (
    `id` bigint NOT NULL COMMENT '主键',
    `username` varchar(100) DEFAULT NULL COMMENT '用户名',
    `password` varchar(100) DEFAULT NULL COMMENT '密码',
    `nickname` varchar(500) DEFAULT NULL COMMENT '用户昵称',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
    `phone` varchar(20) DEFAULT NULL COMMENT '绑定手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '绑定电子邮箱',
    `gender` varchar(500) DEFAULT NULL COMMENT '性别 (0:未知, 1:男, 2:女)',
    `birthday` date DEFAULT NULL COMMENT '出生日期',
    `province` varchar(100) DEFAULT NULL COMMENT '居住省份',
    `city` varchar(100) DEFAULT NULL COMMENT '居住城市',
    `district` varchar(500) DEFAULT NULL COMMENT '居住区/县',
    `sign` varchar(500) DEFAULT NULL COMMENT '个性签名',
    `cooking_level` tinyint DEFAULT NULL COMMENT '厨艺等级 (如 1:初学者, 4:厨艺达人)',
    `status` tinyint DEFAULT NULL COMMENT '账号状态 (1:正常, 0:禁用, -1:已注销)',
    `create_user` int DEFAULT NULL COMMENT '创建用户',
    `create_time` bigint DEFAULT NULL COMMENT '创建时间',
    `update_user` int DEFAULT NULL COMMENT '更新用户',
    `update_time` bigint DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='App用户表';

-- App用户第三方登录授权表
CREATE TABLE IF NOT EXISTS `fd_user_auth` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` bigint DEFAULT NULL COMMENT '用户id',
    `identity_type` varchar(30) DEFAULT NULL COMMENT '登录渠道类型: APPLE, GOOGLE, WECHAT, ALIPAY',
    `identifier` varchar(500) DEFAULT NULL COMMENT '第三方唯一标识 (微信的UnionID/OpenID, Google的Sub, Apple的SubjectID等)',
    `credential` text COMMENT '授权凭证/Token (可选,如需要存三方返回的access_token或临时密匙)',
    `nickname` varchar(100) DEFAULT NULL COMMENT '授权时同步的第三方平台昵称 (备用)',
    `avatar` varchar(500) DEFAULT NULL COMMENT '授权时同步的第三方平台头像 (备用)',
    `create_user` int DEFAULT NULL COMMENT '创建用户',
    `create_time` bigint DEFAULT NULL COMMENT '创建时间',
    `update_user` int DEFAULT NULL COMMENT '更新用户',
    `update_time` bigint DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_identity_identifier` (`identity_type`, `identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='App用户第三方登录授权表';
