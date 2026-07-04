-- ============================================================
-- AI 专属方案配额 - 管理后台菜单
-- 父级：会员运营 (2000100000000000090)
-- 前端：jeecgboot-vue3/src/views/fondia/ai/quota/index.vue
-- 后端：/sys/fd/ai/quota/**
-- ============================================================

INSERT INTO `sys_permission` VALUES (
    '2000100000000000140', '2000100000000000090', 'AI专属方案配额',
    '/fondia/ai/quota', 'fondia/ai/quota/index', 1, NULL, NULL, 1, NULL, '0', 5.00, 0,
    'ant-design:robot-outlined', 0, 1, 0, 0,
    '配置非会员免费次数与各会员等级每日次数', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0
);

INSERT INTO `sys_permission` VALUES (
    '2000100000000000141', '2000100000000000140', '编辑配额', NULL, NULL, 0, NULL, NULL, 2,
    'fondia:aiQuota:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0
);
