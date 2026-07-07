-- Fast16 减脂餐单表结构

CREATE TABLE IF NOT EXISTS `fd_fast_meal_plan` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(64) NOT NULL COMMENT '方案编码',
  `name` varchar(128) NOT NULL COMMENT '方案名称',
  `description` varchar(512) DEFAULT NULL COMMENT '方案说明',
  `target_kcal_min` int DEFAULT NULL COMMENT '建议每日热量下限',
  `target_kcal_max` int DEFAULT NULL COMMENT '建议每日热量上限',
  `is_default` tinyint DEFAULT 0 COMMENT '1=小程序默认方案',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meal_plan_code` (`code`),
  KEY `idx_meal_plan_default` (`is_default`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='减脂餐单方案';

CREATE TABLE IF NOT EXISTS `fd_fast_meal_day` (
  `id` bigint NOT NULL COMMENT '主键',
  `plan_id` bigint NOT NULL COMMENT '方案ID',
  `day_of_week` tinyint NOT NULL COMMENT '1=周一 … 7=周日',
  `day_label` varchar(16) NOT NULL COMMENT '周一/周二…',
  `daily_kcal` int DEFAULT NULL COMMENT '当日总热量',
  `tip` varchar(512) DEFAULT NULL COMMENT '当日提示',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_day` (`plan_id`, `day_of_week`),
  KEY `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='减脂餐单日配置';

CREATE TABLE IF NOT EXISTS `fd_fast_meal_item` (
  `id` bigint NOT NULL COMMENT '主键',
  `plan_id` bigint NOT NULL COMMENT '方案ID',
  `day_id` bigint NOT NULL COMMENT '日配置ID',
  `meal_type` varchar(16) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER',
  `name` varchar(128) NOT NULL COMMENT '餐名',
  `description` varchar(512) DEFAULT NULL COMMENT '做法/配料',
  `kcal` int DEFAULT NULL COMMENT '热量kcal',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签逗号分隔',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_day_meal_type` (`day_id`, `meal_type`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_day_id` (`day_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='减脂餐单餐次';
