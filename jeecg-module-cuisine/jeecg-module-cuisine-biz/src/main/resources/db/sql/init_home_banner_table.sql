-- 首页轮播图表
CREATE TABLE IF NOT EXISTS `fd_home_banner` (
    `id`           bigint        NOT NULL COMMENT '主键',
    `recipe_id`    bigint        DEFAULT NULL COMMENT '关联菜谱ID',
    `title`        varchar(200)  DEFAULT NULL COMMENT '轮播标题（为空则取菜谱标题）',
    `subtitle`     varchar(500)  DEFAULT NULL COMMENT '轮播副标题（为空则取菜谱副标题）',
    `image_url`    varchar(500)  DEFAULT NULL COMMENT '轮播图片（为空则取菜谱封面）',
    `sort_no`      int           NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint       NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `create_by`    varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime      DEFAULT NULL COMMENT '更新时间',
    `sys_org_code` varchar(64)   DEFAULT NULL COMMENT '所属部门',
    `del_flag`     tinyint       NOT NULL DEFAULT 0 COMMENT '删除状态：0正常 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_id` (`recipe_id`),
    KEY `idx_status_sort` (`status`, `sort_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页轮播图表';
