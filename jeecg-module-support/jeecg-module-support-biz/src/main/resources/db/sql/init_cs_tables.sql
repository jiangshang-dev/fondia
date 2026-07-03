-- Fondia 在线客服表结构
CREATE TABLE IF NOT EXISTS `fd_cs_config` (
  `id` bigint NOT NULL COMMENT '主键，固定为1',
  `welcome_title` varchar(100) DEFAULT 'Hi，小厨粉' COMMENT '欢迎标题',
  `welcome_tag` varchar(50) DEFAULT '智能自助' COMMENT '欢迎标签',
  `welcome_desc` varchar(500) DEFAULT NULL COMMENT '欢迎描述',
  `human_greeting` varchar(500) DEFAULT '您好，我是人工客服小美，很高兴为您服务！请问有什么可以帮您的？' COMMENT '人工客服开始语',
  `privacy_tip` varchar(200) DEFAULT '小厨致力保护您的隐私，本次通话已加密。' COMMENT '隐私提示',
  `ai_app_id` varchar(64) DEFAULT NULL COMMENT 'Jeecg AI应用ID（绑定知识库）',
  `transfer_keywords` varchar(200) DEFAULT '人工,转人工,人工客服,召唤人工' COMMENT '转人工关键词，逗号分隔',
  `update_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服全局配置';

CREATE TABLE IF NOT EXISTS `fd_cs_faq` (
  `id` bigint NOT NULL,
  `question` varchar(200) NOT NULL,
  `answer` text NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `sort_no` int DEFAULT 0,
  `status` tinyint DEFAULT 1 COMMENT '0禁用 1启用',
  `create_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cs_faq_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服FAQ';

CREATE TABLE IF NOT EXISTS `fd_cs_quick_entry` (
  `id` bigint NOT NULL,
  `title` varchar(50) NOT NULL,
  `icon` varchar(100) DEFAULT NULL COMMENT '图标key或URL',
  `icon_color` varchar(20) DEFAULT NULL,
  `preset_message` varchar(500) DEFAULT NULL COMMENT '点击后自动发送的消息',
  `sort_no` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `create_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cs_entry_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服快捷入口';

CREATE TABLE IF NOT EXISTS `fd_cs_session` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL COMMENT '接入客服sys_user.id',
  `status` varchar(20) NOT NULL DEFAULT 'AI' COMMENT 'AI/WAITING/CHATTING/CLOSED',
  `source` varchar(30) DEFAULT 'general',
  `ai_conversation_id` varchar(64) DEFAULT NULL COMMENT 'Airag会话ID',
  `last_message` varchar(500) DEFAULT NULL,
  `last_message_time` bigint DEFAULT NULL,
  `user_unread` int DEFAULT 0,
  `agent_unread` int DEFAULT 0,
  `create_time` bigint DEFAULT NULL,
  `close_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cs_session_user_status` (`user_id`, `status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话';

CREATE TABLE IF NOT EXISTS `fd_cs_message` (
  `id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  `sender_type` varchar(10) NOT NULL COMMENT 'USER/AGENT/AI/SYSTEM',
  `sender_id` varchar(64) DEFAULT NULL,
  `msg_type` varchar(20) DEFAULT 'TEXT',
  `content` text,
  `create_time` bigint DEFAULT NULL,
  `client_msg_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cs_message_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息';

-- 默认配置（含 AI 食谱应用 ID，可在管理后台「客服配置」修改）
INSERT INTO `fd_cs_config` (`id`, `welcome_title`, `welcome_tag`, `welcome_desc`, `human_greeting`, `privacy_tip`, `ai_app_id`, `transfer_keywords`, `update_time`)
VALUES (1, 'Hi，小厨粉', '智能自助',
        '我是小厨专属客服，有什么可以帮您的？您可以通过下方自助服务或点击底部直接与我沟通。',
        '您好，我是人工客服小美，很高兴为您服务！请问有什么可以帮您的？🌸',
        '小厨致力保护您的隐私，本次通话已加密。',
        '2072629675695808513',
        '人工,转人工,人工客服,召唤人工', UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

-- 已有库补全 AI 应用 ID（未配置时写入）
UPDATE `fd_cs_config` SET `ai_app_id` = '2072629675695808513'
WHERE `id` = 1 AND (`ai_app_id` IS NULL OR `ai_app_id` = '');

-- 默认FAQ
INSERT INTO `fd_cs_faq` (`id`, `question`, `answer`, `sort_no`, `status`, `create_time`, `update_time`) VALUES
(2000200000000000001, '如何获取更多积分？', '您可以通过每日签到、分享优质菜谱、参与社区互动等方式获得丰厚的积分奖励。', 1, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000200000000000002, '菜谱上传审核需要多久？', '一般情况下，审核团队会在12小时内完成内容合规性审核，请耐心等待。', 2, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000200000000000003, '账号绑定手机号如何更换？', '前往「设置 > 账号与安全 > 手机号绑定」，验证原手机号后即可输入新号码完成更换。', 3, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000200000000000004, '积分商城的商品多久发货？', '实物商品将在兑换后3个工作日内发出，虚拟奖品（如会员、优惠券）将实时到账。', 4, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000200000000000005, '发现恶意抄袭或违规内容如何举报？', '您可以在相关内容详情页点击右上角「...」，选择「举报」，客服会尽快介入处理。', 5, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

-- 默认快捷入口
INSERT INTO `fd_cs_quick_entry` (`id`, `title`, `icon`, `icon_color`, `preset_message`, `sort_no`, `status`, `create_time`, `update_time`) VALUES
(2000210000000000001, '异常处理', 'wallet', '#FF8A4C', '我的账号出现了异常，签到/积分/兑换出现问题，请帮我处理。', 1, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000210000000000002, '账号找回', 'lock_reset', '#4C8AFF', '我想找回/更换绑定的账号信息，请指导我如何操作。', 2, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000210000000000003, '兑换咨询', 'gift', '#FFB300', '我想咨询积分商城兑换与发货相关问题。', 3, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(2000210000000000004, '社区规范', 'gavel', '#00BFA5', '请介绍一下社区内容发布规范与违规处理规则。', 4, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);
