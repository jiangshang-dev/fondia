-- ============================================================
-- Fondia App 管理后台菜单初始化 SQL（MySQL）
-- 说明：可在「系统管理 → 菜单管理」中手动创建，也可直接执行本脚本
-- 前端页面路径：jeecgboot-vue3/src/views/fondia/**
-- 后端接口前缀：/sys/fd/**（需配置网关路由，见同目录 gateway_fondia_routes.json）
-- ============================================================

-- 一级目录：Fondia运营管理
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES ('2000100000000000001', '', 'Fondia运营管理', '/fondia', 'layouts/default/index', 1, NULL, '/fondia/user', 0, NULL, '0', 5.00, 0, 'ant-design:mobile-outlined', 0, 0, 0, 0, 'Fondia App 配套管理后台', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 二级菜单：App用户管理
INSERT INTO `sys_permission` VALUES ('2000100000000000011', '2000100000000000001', 'App用户管理', '/fondia/user', 'fondia/user/index', 1, NULL, NULL, 1, NULL, '0', 1.00, 0, 'ant-design:user-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000012', '2000100000000000011', '编辑用户', NULL, NULL, 0, NULL, NULL, 2, 'fondia:user:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000013', '2000100000000000011', '删除用户', NULL, NULL, 0, NULL, NULL, 2, 'fondia:user:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000014', '2000100000000000011', '禁用/启用', NULL, NULL, 0, NULL, NULL, 2, 'fondia:user:status', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 二级菜单：菜系分类管理
INSERT INTO `sys_permission` VALUES ('2000100000000000021', '2000100000000000001', '菜系分类管理', '/fondia/cuisine/category', 'fondia/cuisine/category/index', 1, NULL, NULL, 1, NULL, '0', 2.00, 0, 'ant-design:appstore-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000022', '2000100000000000021', '新增分类', NULL, NULL, 0, NULL, NULL, 2, 'fondia:category:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000023', '2000100000000000021', '编辑分类', NULL, NULL, 0, NULL, NULL, 2, 'fondia:category:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000024', '2000100000000000021', '删除分类', NULL, NULL, 0, NULL, NULL, 2, 'fondia:category:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 二级菜单：菜谱标签管理
INSERT INTO `sys_permission` VALUES ('2000100000000000031', '2000100000000000001', '菜谱标签管理', '/fondia/cuisine/tag', 'fondia/cuisine/tag/index', 1, NULL, NULL, 1, NULL, '0', 3.00, 0, 'ant-design:tags-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000032', '2000100000000000031', '新增标签', NULL, NULL, 0, NULL, NULL, 2, 'fondia:tag:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000033', '2000100000000000031', '编辑标签', NULL, NULL, 0, NULL, NULL, 2, 'fondia:tag:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000034', '2000100000000000031', '删除标签', NULL, NULL, 0, NULL, NULL, 2, 'fondia:tag:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 二级菜单：菜谱管理
INSERT INTO `sys_permission` VALUES ('2000100000000000041', '2000100000000000001', '菜谱管理', '/fondia/cuisine/recipe', 'fondia/cuisine/recipe/index', 1, NULL, NULL, 1, NULL, '0', 4.00, 0, 'ant-design:read-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000042', '2000100000000000041', '新增菜谱', NULL, NULL, 0, NULL, NULL, 2, 'fondia:recipe:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000043', '2000100000000000041', '编辑菜谱', NULL, NULL, 0, NULL, NULL, 2, 'fondia:recipe:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000044', '2000100000000000041', '删除菜谱', NULL, NULL, 0, NULL, NULL, 2, 'fondia:recipe:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000045', '2000100000000000041', '上下架', NULL, NULL, 0, NULL, NULL, 2, 'fondia:recipe:status', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 二级菜单：评论审核管理
INSERT INTO `sys_permission` VALUES ('2000100000000000051', '2000100000000000001', '评论审核管理', '/fondia/interaction/comment', 'fondia/interaction/comment/index', 1, NULL, NULL, 1, NULL, '0', 5.00, 0, 'ant-design:message-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000052', '2000100000000000051', '审核通过', NULL, NULL, 0, NULL, NULL, 2, 'fondia:comment:publish', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000053', '2000100000000000051', '隐藏评论', NULL, NULL, 0, NULL, NULL, 2, 'fondia:comment:hide', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000054', '2000100000000000051', '删除评论', NULL, NULL, 0, NULL, NULL, 2, 'fondia:comment:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 给 admin 角色授权（role_id 请按实际环境调整，默认 admin 角色 id 通常为 'f6817f48af4fb3af11b9e8bf182f966b'）
-- INSERT INTO sys_role_permission (id, role_id, permission_id) SELECT UUID_SHORT(), 'f6817f48af4fb3af11b9e8bf182f966b', id FROM sys_permission WHERE id LIKE '2000100000000000%';
