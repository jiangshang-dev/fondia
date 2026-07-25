# V14 客服与 AI：会话、SSE、Tool、菜谱卡片

> **所属模块**：M3  ·  **建议时长**：70～90 分钟  ·  **形式**：讲解 + 流式演示
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 3.6

## 一、学习目标

1. 理解会话状态机与消息落库
2. 看懂 SSE 流式调用链
3. 完成「推荐东北菜」流式+卡片实验

## 二、课前准备

- support / agent / search 启动
- 网关 /support/ai/** 指向 jeecg-agent（若已拆分）

## 三、口播 / 讲解提纲

### 3.1 会话

- 创建/活跃/历史
- source=ai_tab 与 general 隔离

### 3.2 SSE

- text/event-stream
- 事件：MESSAGE / APP_META / APP_ERROR / APP_LINK

### 3.3 Tool

- 搜索菜谱工具
- 结束时推送卡片元数据

## 四、屏幕演示步骤

1. 走读 FdCsSessionServiceImpl / SupportAiController
2. 演示 SSE：MESSAGE → APP_META
3. 展示 SearchTool / 卡片 deep link

## 五、课堂实验 / 作业

1. 提问「推荐一道东北菜」
2. 确认流式输出与可点击菜谱卡片
3. 检查消息 extraJson 是否保存 recipes

## 六、本节小结（片尾 1 分钟）

- 会话在 support/DB，推理可在 agent
- 卡片要持久化，避免刷新丢失

**下节预告**：V15 会员与支付


## 七、关键文件 / 参考

- `jeecg-module-support/.../FdCsSessionServiceImpl.java`
- `jeecg-module-ai/.../SupportAiController.java`
- `db/alter_fd_cs_message_extra_json.sql`
