-- ============================================================
-- Fondia 菜谱/菜品模块表结构 + 示例数据
-- ============================================================

-- 1. 菜谱分类表
CREATE TABLE IF NOT EXISTS `fd_recipe_category` (
    `id`              bigint        NOT NULL COMMENT '主键',
    `parent_id`       bigint        NOT NULL DEFAULT 0 COMMENT '父级ID，0表示顶级菜系',
    `name`            varchar(100)  NOT NULL COMMENT '分类名称',
    `category_level`  tinyint       NOT NULL COMMENT '层级：1=菜系，2=子分类(主食/菜)',
    `sub_type`        tinyint       DEFAULT NULL COMMENT '子分类类型(L2有效)：1=主食，2=菜',
    `icon`            varchar(500)  DEFAULT NULL COMMENT '图标',
    `cover_image`     varchar(500)  DEFAULT NULL COMMENT '封面图',
    `description`     varchar(1000) DEFAULT NULL COMMENT '分类描述',
    `feature_tags`    varchar(500)  DEFAULT NULL COMMENT '特色标签JSON',
    `is_hot`          tinyint       NOT NULL DEFAULT 0 COMMENT '是否热门菜系',
    `sort_no`         int           NOT NULL DEFAULT 0 COMMENT '排序号',
    `status`          tinyint       NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `create_by`       varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`    varchar(64)   DEFAULT NULL COMMENT '所属部门',
    `del_flag`        tinyint       NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level_hot` (`category_level`, `is_hot`, `del_flag`),
    KEY `idx_sub_type` (`parent_id`, `sub_type`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱分类表';

-- 2. 标签字典表
CREATE TABLE IF NOT EXISTS `fd_recipe_tag` (
    `id`           bigint       NOT NULL COMMENT '主键',
    `name`         varchar(50)  NOT NULL COMMENT '标签名',
    `tag_type`     varchar(30)  DEFAULT NULL COMMENT '标签类型',
    `sort_no`      int          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `create_by`    varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64)  DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint      NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name_del` (`name`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱标签字典';

-- 3. 菜谱主表
CREATE TABLE IF NOT EXISTS `fd_recipe` (
    `id`              bigint        NOT NULL COMMENT '主键',
    `category_id`     bigint        NOT NULL COMMENT '所属子分类ID(L2)',
    `cuisine_id`      bigint        NOT NULL COMMENT '所属菜系ID(L1)',
    `title`           varchar(200)  NOT NULL COMMENT '菜谱标题',
    `subtitle`        varchar(500)  DEFAULT NULL COMMENT '副标题',
    `description`     varchar(2000) DEFAULT NULL COMMENT '详情描述',
    `cover_image`     varchar(500)  DEFAULT NULL COMMENT '封面图',
    `video_url`       varchar(500)  DEFAULT NULL COMMENT '视频地址',
    `video_duration`  int           DEFAULT NULL COMMENT '视频时长(秒)',
    `difficulty`      tinyint       NOT NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3困难',
    `cook_minutes`    int           DEFAULT NULL COMMENT '烹饪时长(分钟)',
    `calories`        int           DEFAULT NULL COMMENT '热量',
    `serving_size`    varchar(50)   DEFAULT NULL COMMENT '份量说明',
    `like_count`      int           NOT NULL DEFAULT 0 COMMENT '点赞数',
    `collect_count`   int           NOT NULL DEFAULT 0 COMMENT '收藏数',
    `comment_count`   int           NOT NULL DEFAULT 0 COMMENT '评论数',
    `view_count`      int           NOT NULL DEFAULT 0 COMMENT '浏览数',
    `is_recommend`    tinyint       NOT NULL DEFAULT 0 COMMENT '是否推荐',
    `sort_no`         int           NOT NULL DEFAULT 0 COMMENT '排序',
    `status`          tinyint       NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    `publish_time`    datetime      DEFAULT NULL COMMENT '发布时间',
    `create_by`       varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`    varchar(64)   DEFAULT NULL COMMENT '所属部门',
    `del_flag`        tinyint       NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`, `status`, `del_flag`),
    KEY `idx_cuisine_sub` (`cuisine_id`, `category_id`, `del_flag`),
    KEY `idx_recommend` (`is_recommend`, `sort_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱主表';

-- 4. 菜谱-标签关联表
CREATE TABLE IF NOT EXISTS `fd_recipe_tag_rel` (
    `id`           bigint      NOT NULL COMMENT '主键',
    `recipe_id`    bigint      NOT NULL COMMENT '菜谱ID',
    `tag_id`       bigint      NOT NULL COMMENT '标签ID',
    `create_by`    varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime    DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime    DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint     NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_recipe_tag` (`recipe_id`, `tag_id`, `del_flag`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱标签关联表';

-- 5. 食材表
CREATE TABLE IF NOT EXISTS `fd_recipe_ingredient` (
    `id`           bigint       NOT NULL COMMENT '主键',
    `recipe_id`    bigint       NOT NULL COMMENT '菜谱ID',
    `name`         varchar(100) NOT NULL COMMENT '食材名称',
    `amount`       varchar(50)  NOT NULL COMMENT '用量',
    `image`        varchar(500) DEFAULT NULL COMMENT '食材图片',
    `sort_no`      int          NOT NULL DEFAULT 0 COMMENT '排序',
    `create_by`    varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64)  DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint      NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_sort` (`recipe_id`, `sort_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱食材表';

-- 6. 步骤表
CREATE TABLE IF NOT EXISTS `fd_recipe_step` (
    `id`           bigint        NOT NULL COMMENT '主键',
    `recipe_id`    bigint        NOT NULL COMMENT '菜谱ID',
    `step_no`      int           NOT NULL COMMENT '步骤序号',
    `image`        varchar(500)  DEFAULT NULL COMMENT '步骤图片',
    `content`      varchar(2000) NOT NULL COMMENT '步骤说明',
    `tip`          varchar(500)  DEFAULT NULL COMMENT '小贴士',
    `sort_no`      int           NOT NULL DEFAULT 0 COMMENT '排序',
    `create_by`    varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime      DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64)   DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint       NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_step` (`recipe_id`, `step_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱步骤表';

-- 示例数据
INSERT INTO `fd_recipe_category` (`id`, `parent_id`, `name`, `category_level`, `sub_type`, `icon`, `cover_image`, `description`, `feature_tags`, `is_hot`, `sort_no`, `status`, `del_flag`, `create_time`)
VALUES
(1001, 0, '东北菜', 1, NULL, '❄️', 'lib/assets/addr/dongbei.png',
 '东北菜以炖、焖、酱、熏为主要烹饪方式，口味咸鲜浓厚，食材丰富实在。',
 '["特色:咸鲜浓郁","烹饪:炖/焖/酱/熏","代表食材:猪肉/酸菜/玉米"]', 1, 1, 1, 0, NOW()),
(1002, 0, '川菜', 1, NULL, '🌶️', NULL, '川菜以麻辣鲜香著称。', NULL, 1, 2, 1, 0, NOW());

INSERT INTO `fd_recipe_category` (`id`, `parent_id`, `name`, `category_level`, `sub_type`, `sort_no`, `status`, `del_flag`, `create_time`)
VALUES
(1101, 1001, '主食', 2, 1, 1, 1, 0, NOW()),
(1102, 1001, '菜',   2, 2, 2, 1, 0, NOW());

INSERT INTO `fd_recipe_tag` (`id`, `name`, `tag_type`, `sort_no`, `status`, `del_flag`, `create_time`)
VALUES
(2001, '东北菜', 'CUISINE', 1, 1, 0, NOW()),
(2002, '家常菜', 'STYLE',   2, 1, 0, NOW()),
(2003, '炖菜',   'STYLE',   3, 1, 0, NOW()),
(2004, '咸香味', 'TASTE',   4, 1, 0, NOW());

INSERT INTO `fd_recipe` (`id`, `category_id`, `cuisine_id`, `title`, `subtitle`, `description`, `difficulty`, `cook_minutes`, `calories`, `serving_size`, `status`, `del_flag`, `create_time`, `publish_time`)
VALUES
(3001, 1102, 1001, '红烧肉', '肥而不腻，软糯入味，经典家常菜',
 '肥而不腻，软糯入味，经典东北家常菜。', 2, 60, 620, '2-3人份', 1, 0, NOW(), NOW());

INSERT INTO `fd_recipe_tag_rel` (`id`, `recipe_id`, `tag_id`, `del_flag`, `create_time`)
VALUES
(4001, 3001, 2001, 0, NOW()),
(4002, 3001, 2002, 0, NOW()),
(4003, 3001, 2003, 0, NOW()),
(4004, 3001, 2004, 0, NOW());

INSERT INTO `fd_recipe_ingredient` (`id`, `recipe_id`, `name`, `amount`, `sort_no`, `del_flag`, `create_time`)
VALUES
(5001, 3001, '五花肉', '500克', 1, 0, NOW()),
(5002, 3001, '冰糖',   '20克',  2, 0, NOW()),
(5003, 3001, '生姜',   '4片',   3, 0, NOW()),
(5004, 3001, '大葱',   '1根',   4, 0, NOW()),
(5005, 3001, '料酒',   '2勺',   5, 0, NOW()),
(5006, 3001, '老抽',   '1勺',   6, 0, NOW());

INSERT INTO `fd_recipe_step` (`id`, `recipe_id`, `step_no`, `content`, `tip`, `sort_no`, `del_flag`, `create_time`)
VALUES
(6001, 3001, 1, '五花肉切麻将块，冷水下锅，加入姜片、料酒，焯水去腥，捞出沥干。', '焯水时间：5分钟', 1, 0, NOW()),
(6002, 3001, 2, '锅中放少许油，加入冰糖，小火炒至融化，变成枣红色。', '小火慢炒，注意别糊', 2, 0, NOW());
