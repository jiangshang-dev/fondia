-- ============================================================
-- Fondia 会员/积分/商城 管理后台菜单（MySQL）
-- 父级：Fondia运营管理 (2000100000000000001)
-- 前端页面：jeecgboot-vue3/src/views/fondia/member|points|mall/**
-- 后端接口：/sys/fd/member/**、/sys/fd/points/**、/sys/fd/mall/**
-- 网关路由：见 gateway_fondia_routes.json → lb://jeecg-member
-- ============================================================

-- ========== 二级目录：会员运营 ==========
INSERT INTO `sys_permission` VALUES ('2000100000000000090', '2072537385652469761', '会员运营', '/fondia/member', 'layouts/default/index', 1, NULL, '/fondia/member/plan', 0, NULL, '0', 7.00, 0, 'ant-design:crown-outlined', 0, 0, 0, 0, '会员套餐/订单/订阅/支付', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000091', '2072537385652469761', '会员套餐', '/fondia/member/plan', 'fondia/member/plan/index', 1, NULL, NULL, 1, NULL, '0', 1.00, 0, 'ant-design:gift-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000092', '2072537385652469761', '新增套餐', NULL, NULL, 0, NULL, NULL, 2, 'fondia:memberPlan:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000093', '2072537385652469761', '编辑套餐', NULL, NULL, 0, NULL, NULL, 2, 'fondia:memberPlan:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000094', '2072537385652469761', '删除套餐', NULL, NULL, 0, NULL, NULL, 2, 'fondia:memberPlan:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000095', '2072537385652469761', '上下架', NULL, NULL, 0, NULL, NULL, 2, 'fondia:memberPlan:status', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000096', '2072537385652469761', '会员订单', '/fondia/member/order', 'fondia/member/order/index', 1, NULL, NULL, 1, NULL, '0', 2.00, 0, 'ant-design:shopping-outlined', 1, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000097', '2072537385652469761', '查看详情', NULL, NULL, 0, NULL, NULL, 2, 'fondia:memberOrder:view', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000098', '2000100000000000090', '会员订阅', '/fondia/member/subscription', 'fondia/member/subscription/index', 1, NULL, NULL, 1, NULL, '0', 3.00, 0, 'ant-design:idcard-outlined', 1, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000099', '2000100000000000098', '查看详情', NULL, NULL, 0, NULL, NULL, 2, 'fondia:memberSubscription:view', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000100', '2000100000000000090', '支付流水', '/fondia/member/payment', 'fondia/member/payment/index', 1, NULL, NULL, 1, NULL, '0', 4.00, 0, 'ant-design:transaction-outlined', 1, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000101', '2000100000000000100', '查看详情', NULL, NULL, 0, NULL, NULL, 2, 'fondia:payment:view', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- ========== 二级目录：积分运营 ==========
INSERT INTO `sys_permission` VALUES ('2000100000000000110', '2000100000000000001', '积分运营', '/fondia/points', 'layouts/default/index', 1, NULL, '/fondia/points/ledger', 0, NULL, '0', 8.00, 0, 'ant-design:gold-outlined', 0, 0, 0, 0, '积分流水/任务/签到', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000111', '2000100000000000110', '积分流水', '/fondia/points/ledger', 'fondia/points/ledger/index', 1, NULL, NULL, 1, NULL, '0', 1.00, 0, 'ant-design:unordered-list-outlined', 1, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000112', '2000100000000000111', '查看详情', NULL, NULL, 0, NULL, NULL, 2, 'fondia:pointsLedger:view', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000113', '2000100000000000111', '运营调账', NULL, NULL, 0, NULL, NULL, 2, 'fondia:pointsLedger:adjust', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000114', '2000100000000000110', '积分任务', '/fondia/points/task', 'fondia/points/task/index', 1, NULL, NULL, 1, NULL, '0', 2.00, 0, 'ant-design:check-circle-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000115', '2000100000000000114', '新增任务', NULL, NULL, 0, NULL, NULL, 2, 'fondia:pointsTask:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000116', '2000100000000000114', '编辑任务', NULL, NULL, 0, NULL, NULL, 2, 'fondia:pointsTask:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000117', '2000100000000000114', '删除任务', NULL, NULL, 0, NULL, NULL, 2, 'fondia:pointsTask:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000118', '2000100000000000114', '启用/禁用', NULL, NULL, 0, NULL, NULL, 2, 'fondia:pointsTask:status', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000119', '2000100000000000110', '签到规则', '/fondia/points/checkinConfig', 'fondia/points/checkinConfig/index', 1, NULL, NULL, 1, NULL, '0', 3.00, 0, 'ant-design:calendar-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000120', '2000100000000000119', '编辑规则', NULL, NULL, 0, NULL, NULL, 2, 'fondia:checkinConfig:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000121', '2000100000000000110', '签到记录', '/fondia/points/checkinRecord', 'fondia/points/checkinRecord/index', 1, NULL, NULL, 1, NULL, '0', 4.00, 0, 'ant-design:history-outlined', 1, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000122', '2000100000000000121', '查看详情', NULL, NULL, 0, NULL, NULL, 2, 'fondia:checkinRecord:view', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- ========== 二级目录：积分商城 ==========
INSERT INTO `sys_permission` VALUES ('2000100000000000130', '2000100000000000001', '积分商城', '/fondia/mall', 'layouts/default/index', 1, NULL, '/fondia/mall/product', 0, NULL, '0', 9.00, 0, 'ant-design:shop-outlined', 0, 0, 0, 0, '商城商品与兑换订单', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000131', '2000100000000000130', '商城商品', '/fondia/mall/product', 'fondia/mall/product/index', 1, NULL, NULL, 1, NULL, '0', 1.00, 0, 'ant-design:skin-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000132', '2000100000000000131', '新增商品', NULL, NULL, 0, NULL, NULL, 2, 'fondia:mallProduct:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000133', '2000100000000000131', '编辑商品', NULL, NULL, 0, NULL, NULL, 2, 'fondia:mallProduct:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000134', '2000100000000000131', '删除商品', NULL, NULL, 0, NULL, NULL, 2, 'fondia:mallProduct:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000135', '2000100000000000131', '上下架', NULL, NULL, 0, NULL, NULL, 2, 'fondia:mallProduct:status', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission` VALUES ('2000100000000000136', '2000100000000000130', '兑换订单', '/fondia/mall/order', 'fondia/mall/order/index', 1, NULL, NULL, 1, NULL, '0', 2.00, 0, 'ant-design:file-done-outlined', 0, 1, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000137', '2000100000000000136', '查看详情', NULL, NULL, 0, NULL, NULL, 2, 'fondia:mallOrder:view', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('2000100000000000138', '2000100000000000136', '发货/完成', NULL, NULL, 0, NULL, NULL, 2, 'fondia:mallOrder:ship', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- 给 admin 角色授权（按需取消注释）
-- INSERT INTO sys_role_permission (id, role_id, permission_id)
-- SELECT REPLACE(UUID(), '-', ''), 'f6817f48af4fb3af11b9e8bf182f966b', id
-- FROM sys_permission WHERE id LIKE '200010000000000009%' OR id LIKE '200010000000000011%' OR id LIKE '200010000000000013%';
