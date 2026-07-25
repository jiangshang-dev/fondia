# V16 网关路由与跨服务 Feign 鉴权

> **所属模块**：M3  ·  **建议时长**：30～45 分钟  ·  **形式**：讲解 + 配置实操
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 3.8

## 一、学习目标

1. 把 gateway_fondia_routes.json 导入 Nacos
2. 理解 Authorization 透传
3. 排查 502/404 路由问题

## 二、课前准备

- Nacos 控制台账号
- 打开路由 JSON

## 三、口播 / 讲解提纲

### 3.1 路由

- lb://服务名
- order 优先级
- WS 用 lb:ws://

### 3.2 Feign

- contextId 防冲突
- 转发 Authorization

### 3.3 排错

- Nacos 未注册
- 路由未刷新
- 路径被更宽规则抢走

## 四、屏幕演示步骤

1. 导入/更新 jeecg-gateway-router
2. 对比 /support/ai/** 与 /support/** order
3. 抓包看 Feign 头

## 五、课堂实验 / 作业

1. 故意写错 uri 制造 502，再修复
2. 确认 AI 路径打到正确服务

## 六、本节小结（片尾 1 分钟）

- 更具体的 Path 必须更高优先级
- 跨服务要带上用户身份

**下节预告**：M4 V17 Airag 与提示词


## 七、关键文件 / 参考

- `db/gateway_fondia_routes.json`
