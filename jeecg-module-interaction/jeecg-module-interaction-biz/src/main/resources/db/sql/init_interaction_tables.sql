-- ============================================================
-- Fondia 互动模块表结构（评论/点赞/收藏/浏览记录）
-- ============================================================

-- ------------------------------------------------------------
-- 1. 菜谱评论表（多级评论）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_recipe_comment` (
    `id`              bigint        NOT NULL COMMENT '主键',
    `recipe_id`       bigint        NOT NULL COMMENT '菜谱ID',
    `user_id`         bigint        NOT NULL COMMENT '评论用户ID(fd_user.id)',
    `parent_id`       bigint        NOT NULL DEFAULT 0 COMMENT '父评论ID，0为一级评论',
    `root_id`         bigint        NOT NULL DEFAULT 0 COMMENT '根评论ID，一级评论等于自身id',
    `reply_user_id`   bigint        DEFAULT NULL COMMENT '被回复用户ID',
    `content`         varchar(2000) NOT NULL COMMENT '评论内容',
    `like_count`      int           NOT NULL DEFAULT 0 COMMENT '点赞数(冗余)',
    `dislike_count`   int           NOT NULL DEFAULT 0 COMMENT '点踩数(冗余)',
    `reply_count`     int           NOT NULL DEFAULT 0 COMMENT '回复数(冗余)',
    `ip`              varchar(64)   DEFAULT NULL COMMENT '评论IP',
    `status`          tinyint       NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1正常 2审核中',
    `create_by`       varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`    varchar(64)   DEFAULT NULL COMMENT '所属部门',
    `del_flag`        tinyint       NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_root` (`recipe_id`, `root_id`, `del_flag`),
    KEY `idx_parent` (`parent_id`, `del_flag`),
    KEY `idx_user` (`user_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱评论表';

-- ------------------------------------------------------------
-- 2. 评论点赞/点踩表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_recipe_comment_reaction` (
    `id`             bigint      NOT NULL COMMENT '主键',
    `comment_id`     bigint      NOT NULL COMMENT '评论ID',
    `user_id`        bigint      NOT NULL COMMENT '用户ID',
    `reaction_type`  tinyint     NOT NULL COMMENT '反应类型：1点赞 2点踩',
    `create_by`      varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time`    datetime    DEFAULT NULL COMMENT '创建时间',
    `update_by`      varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time`    datetime    DEFAULT NULL COMMENT '更新时间',
    `sys_org_code`   varchar(64) DEFAULT NULL COMMENT '所属部门',
    `del_flag`       tinyint     NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`, `del_flag`),
    KEY `idx_user` (`user_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞点踩表';

-- ------------------------------------------------------------
-- 3. 菜谱收藏表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_recipe_collect` (
    `id`           bigint      NOT NULL COMMENT '主键',
    `user_id`      bigint      NOT NULL COMMENT '用户ID',
    `recipe_id`    bigint      NOT NULL COMMENT '菜谱ID',
    `create_by`    varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime    DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime    DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint     NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_recipe` (`user_id`, `recipe_id`, `del_flag`),
    KEY `idx_recipe` (`recipe_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱收藏表';

-- ------------------------------------------------------------
-- 4. 菜谱点赞表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_recipe_like` (
    `id`           bigint      NOT NULL COMMENT '主键',
    `user_id`      bigint      NOT NULL COMMENT '用户ID',
    `recipe_id`    bigint      NOT NULL COMMENT '菜谱ID',
    `create_by`    varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime    DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime    DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint     NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_recipe` (`user_id`, `recipe_id`, `del_flag`),
    KEY `idx_recipe` (`recipe_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱点赞表';

-- ------------------------------------------------------------
-- 5. 浏览记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fd_recipe_browse_history` (
    `id`           bigint      NOT NULL COMMENT '主键',
    `user_id`      bigint      NOT NULL COMMENT '用户ID',
    `recipe_id`    bigint      NOT NULL COMMENT '菜谱ID',
    `browse_time`  datetime    NOT NULL COMMENT '最近浏览时间',
    `create_by`    varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime    DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime    DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint     NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_recipe` (`user_id`, `recipe_id`, `del_flag`),
    KEY `idx_user_time` (`user_id`, `browse_time`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜谱浏览记录表';
