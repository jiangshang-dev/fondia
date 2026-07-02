# Fondia 在线客服功能设计文档（WebSocket）

> 版本：v1.0  
> 适用：Flutter App + JeecgBoot 微服务 + Vue3 管理后台  
> 现状：`OnlineService.dart`（FAQ 静态）、`HumanService.dart`（聊天 Mock），后端尚无客服模块

---

## 1. 目标与范围

### 1.1 业务目标

| 能力 | 说明 |
|------|------|
| 智能自助 | FAQ 折叠列表、快捷入口（异常处理/账号找回等） |
| 人工客服 | 用户发起会话 → WebSocket 实时聊天 → 客服工作台接待 |
| 消息持久化 | 聊天记录入库，支持断线重连后拉取历史 |
| 后台运营 | FAQ/快捷入口配置、会话监控、客服工作台、数据统计 |

### 1.2 本期范围（MVP）

- ✅ App 端：FAQ 接口化 + 创建会话 + WebSocket 聊天页
- ✅ 后端：会话/消息 REST + WebSocket 推送 + Redis 多节点广播
- ✅ 管理端：FAQ 管理、会话列表、客服工作台（单窗口聊天）
- ❌ 暂不包含：AI 机器人、多客服抢单算法、语音/视频、工单系统

### 1.3 不在本期

- 与 Jeecg 系统通知 WebSocket（`/websocket/{userId}`）混用——该通道面向后台用户通知，协议/鉴权/路由均不同，**需独立建客服 WS 通道**。

---

## 2. 总体架构

```
┌─────────────────┐     REST + WS      ┌──────────────────┐
│  Flutter App    │◄──────────────────►│  API Gateway     │
│  OnlineService  │   /support/**      │  (9999)          │
│  HumanService   │   ws://.../ws/cs   │  ws 路由需单独配  │
└─────────────────┘                    └────────┬─────────┘
                                                  │
                    ┌─────────────────────────────┼─────────────────────────────┐
                    ▼                             ▼                             ▼
           ┌────────────────┐            ┌────────────────┐            ┌────────────────┐
           │ jeecg-user     │            │ jeecg-support  │  (新建)    │ jeecg-system   │
           │ 用户鉴权/资料   │            │ 客服核心服务    │            │ 后台客服账号    │
           └────────────────┘            └───────┬────────┘            └────────────────┘
                                                 │
                                    ┌────────────┴────────────┐
                                    ▼                         ▼
                              ┌──────────┐            ┌──────────┐
                              │  MySQL   │            │  Redis   │
                              │ 会话/消息 │            │ 在线/广播 │
                              └──────────┘            └──────────┘

┌─────────────────┐     REST + WS      ┌──────────────────┐
│ Vue3 管理后台    │◄──────────────────►│ /sys/fd/cs/**    │
│ FAQ 管理        │                    │ 客服工作台        │
│ 客服工作台       │                    └──────────────────┘
└─────────────────┘
```

### 2.1 模块归属建议

**推荐新建 `jeecg-module-support`（客服模块）**，与 user/cuisine/interaction 并列：

```
jeecg-module-support/
├── jeecg-module-support-api/
├── jeecg-module-support-biz/
│   ├── entity/          会话、消息、FAQ、快捷入口
│   ├── websocket/       CsWebSocketEndpoint
│   ├── service/
│   ├── controller/      App REST
│   └── admin/controller/ 管理 REST
└── jeecg-support-cloud-start/
```

**理由**：客服含 WS 长连接、会话状态机、客服工作台，职责独立；避免 user 模块过于臃肿。

**Phase 1 可折中**：若希望少起一个微服务，可先放在 `jeecg-module-user` 的 `support` 包下，接口前缀仍用 `/support/**`，后续再拆。

---

## 3. 核心流程

### 3.1 用户侧（App）

```
进入「在线客服」页
    │
    ├─ GET /support/faq/list          → 渲染 FAQ + 快捷入口
    │
    └─ 点击「召唤人工客服」
           │
           ├─ POST /support/session/create  → 返回 sessionId
           │
           ├─ WebSocket 连接 ws://host/ws/cs/app/{sessionId}
           │     Header: Authorization: Bearer {token}
           │
           ├─ GET /support/session/{id}/messages?pageNo=1  → 历史消息
           │
           └─ 发送消息（WS 上行）→ 服务端落库 → 推送给客服（WS 下行）
```

### 3.2 客服侧（管理后台）

```
客服登录 Jeecg 后台 → 打开「客服工作台」
    │
    ├─ WebSocket 连接 ws://host/ws/cs/agent/{sysUserId}
    │
    ├─ GET /sys/fd/cs/session/list?status=WAITING  → 待接入列表
    │
    ├─ POST /sys/fd/cs/session/accept?id=         → 接入会话
    │
    └─ 聊天窗口收发消息（WS + 历史 REST 兜底）
```

### 3.3 会话状态机

```
CREATED → WAITING → CHATTING → CLOSED
           ↑           │
           └─ 超时未接入 ─┘
```

| 状态 | 含义 |
|------|------|
| CREATED | 刚创建，尚未连 WS |
| WAITING | 用户在线等待客服 |
| CHATTING | 客服已接入 |
| CLOSED | 用户/客服/系统关闭 |

---

## 4. 数据库设计

### 4.1 表结构（`fd_cs_` 前缀）

#### `fd_cs_faq` — 常见问题

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| question | varchar(200) | 问题 |
| answer | text | 答案 |
| category | varchar(50) | 分类（可选） |
| sort_no | int | 排序 |
| status | tinyint | 0禁用 1启用 |

#### `fd_cs_quick_entry` — 快捷入口

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| title | varchar(50) | 如「异常处理」 |
| icon | varchar(100) | 图标 key / URL |
| entry_type | varchar(30) | FAQ_JUMP / SESSION_WITH_TAG |
| target_value | varchar(200) | 跳转 FAQ id 或会话标签 |
| sort_no | int | 排序 |
| status | tinyint | 状态 |

#### `fd_cs_session` — 会话

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键（即 sessionId） |
| user_id | bigint | App 用户 ID |
| agent_id | varchar(32) | 接入客服（sys_user.id） |
| status | varchar(20) | CREATED/WAITING/CHATTING/CLOSED |
| source | varchar(30) | 来源：general / account / points 等 |
| last_message | varchar(500) | 最后一条消息摘要 |
| last_message_time | bigint | 最后消息时间 |
| user_unread | int | 用户未读数 |
| agent_unread | int | 客服未读数 |
| create_time | bigint | 创建时间 |
| close_time | bigint | 关闭时间 |

#### `fd_cs_message` — 消息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| session_id | bigint | 会话 ID |
| sender_type | varchar(10) | USER / AGENT / SYSTEM |
| sender_id | varchar(64) | 用户 id 或客服 id |
| msg_type | varchar(20) | TEXT / IMAGE |
| content | text | 文本或图片 URL |
| create_time | bigint | 发送时间 |
| client_msg_id | varchar(64) | 客户端幂等 id（防重发） |

**索引建议**：
- `fd_cs_session(user_id, status, create_time)`
- `fd_cs_message(session_id, create_time)`

---

## 5. WebSocket 协议设计

### 5.1 连接地址

| 端 | URL | 鉴权 |
|----|-----|------|
| App | `ws://{gateway}/ws/cs/app/{sessionId}` | Bearer Token（握手 Header 或 `Sec-WebSocket-Protocol`） |
| 管理端 | `ws://{gateway}/ws/cs/agent/{agentId}` | Jeecg 后台 Token |

> 网关需配置 WebSocket 路由（参考 Nacos 中 `jeecg-system-websocket`），**不能走普通 HTTP 负载均衡超时**。

### 5.2 消息 envelope（统一 JSON）

```json
{
  "cmd": "CHAT_SEND",
  "sessionId": "2072344443836252162",
  "data": { },
  "timestamp": 1782920668851
}
```

### 5.3 指令清单

| cmd | 方向 | 说明 |
|-----|------|------|
| `PING` / `PONG` | 双向 | 心跳（55s 间隔，与 Jeecg 前端一致） |
| `CHAT_SEND` | 上行 | 发送聊天消息 |
| `CHAT_ACK` | 下行 | 消息已入库确认（含 serverMsgId） |
| `CHAT_PUSH` | 下行 | 推送新消息给对方 |
| `SESSION_WAITING` | 下行 | 排队中（暂无客服） |
| `SESSION_ACCEPTED` | 下行 | 客服已接入 |
| `SESSION_CLOSED` | 下行 | 会话结束 |
| `READ_RECEIPT` | 双向 | 已读回执（可选 MVP 后做） |

### 5.4 发送消息示例

**上行（用户发文本）**：
```json
{
  "cmd": "CHAT_SEND",
  "sessionId": "2072344443836252162",
  "data": {
    "clientMsgId": "uuid-xxx",
    "msgType": "TEXT",
    "content": "签到失败了怎么办？"
  }
}
```

**下行（推送给客服）**：
```json
{
  "cmd": "CHAT_PUSH",
  "sessionId": "2072344443836252162",
  "data": {
    "id": "2072344443836253001",
    "senderType": "USER",
    "msgType": "TEXT",
    "content": "签到失败了怎么办？",
    "createTime": 1782920668851
  }
}
```

### 5.5 多实例与 Redis

参考 Jeecg 现有 `WebSocket.java` + `REDIS_TOPIC_NAME` 模式：

```
实例 A 收到用户消息 → 落库 → 发布 Redis Topic(cs:socket)
实例 B 订阅 → 若客服 WS 在本实例 → pushMessage
```

**在线映射 Redis Key**：
- `cs:online:user:{userId}` → sessionId
- `cs:online:agent:{agentId}` → 1
- `cs:session:ws:{sessionId}` → 当前实例 nodeId（可选）

---

## 6. REST API 设计

### 6.1 App 端（`/support/**` → jeecg-support / jeecg-user）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/support/faq/list` | FAQ 列表（匿名可访问） |
| GET | `/support/quick-entry/list` | 快捷入口 |
| POST | `/support/session/create` | 创建会话 `{ source?: string }` |
| GET | `/support/session/active` | 当前用户进行中的会话 |
| GET | `/support/session/{id}/messages` | 分页历史消息 |
| POST | `/support/session/{id}/close` | 用户结束会话 |
| POST | `/support/upload` | 聊天图片（可复用 `/user/upload?biz=cs`） |

### 6.2 管理端（`/sys/fd/cs/**`）

| 方法 | 路径 | 说明 |
|------|------|------|
| **FAQ** | | |
| GET/POST/PUT/DELETE | `/sys/fd/cs/faq/*` | CRUD（同 Fondia 其他模块） |
| **快捷入口** | | |
| GET/POST/PUT/DELETE | `/sys/fd/cs/quickEntry/*` | CRUD |
| **会话** | | |
| GET | `/sys/fd/cs/session/list` | 会话列表（筛选 status/时间） |
| GET | `/sys/fd/cs/session/queryById` | 会话详情 + 用户信息 |
| POST | `/sys/fd/cs/session/accept` | 客服接入 |
| POST | `/sys/fd/cs/session/close` | 客服关闭 |
| GET | `/sys/fd/cs/session/messages` | 某会话消息分页 |
| **统计（可选）** | | |
| GET | `/sys/fd/cs/stats/overview` | 今日会话数、平均响应时长 |

---

## 7. 管理后台功能设计

### 7.1 菜单结构（Fondia运营管理 下）

```
Fondia运营管理
├── ...
├── 意见反馈管理        （已有）
└── 在线客服
    ├── FAQ 管理         /fondia/support/faq
    ├── 快捷入口管理      /fondia/support/quickEntry
    └── 客服工作台        /fondia/support/workbench
```

### 7.2 FAQ 管理页

- 列表：问题、分类、排序、状态
- 弹窗：新增/编辑 question + answer
- 与 App `OnlineService` FAQ 区域对接

### 7.3 快捷入口管理页

- 配置四个金刚区图标、标题、行为（跳 FAQ / 带标签进会话）

### 7.4 客服工作台（核心）

```
┌────────────────────────────────────────────────────────────┐
│ 左侧：会话列表                │ 右侧：聊天窗口                │
│ [待接入] [进行中] [已结束]     │  用户信息：昵称/手机号         │
│ ─────────────────            │  ─────────────────────       │
│ ● 小厨粉  14:32  签到问题...   │  [消息气泡列表]              │
│ ○ 用户A   13:10  账号找回...   │                              │
│                              │  [输入框] [图片] [发送]       │
│                              │  [接入] [结束会话]             │
└────────────────────────────────────────────────────────────┘
```

**功能点**：
- WebSocket 长连接 + 新消息声音/角标
- 待接入队列：一键「接入」
- 消息列表虚拟滚动 + 上拉加载历史（REST 兜底）
- 图片消息：上传 + 预览（复用 JUpload / App upload）
- 结束会话后状态同步 App

**权限**：
- `fondia:cs:faq:*` / `fondia:cs:workbench` / `fondia:cs:session:accept`

**客服身份**：使用 Jeecg 后台 `sys_user`，通过角色「Fondia客服」控制工作台入口。

---

## 8. Flutter App 改造要点

### 8.1 页面映射

| 现有页面 | 改造 |
|----------|------|
| `OnlineService.dart` | FAQ/快捷入口改接口；底部按钮创建会话并跳转 |
| `HumanService.dart` | 接入 WebSocket + 真实消息列表 |

### 8.2 依赖

```yaml
web_socket_channel: ^3.0.0   # 或 dio 5.x 的 ws 支持
```

### 8.3 客户端模块

```
lib/
├── api/support.dart           # REST
├── services/cs_websocket.dart # WS 连接、重连、心跳
├── models/cs_models.dart      # Session, CsMessage, Faq
└── pages/Mine/
    ├── OnlineService.dart
    └── HumanService.dart      # 增加 sessionId 参数
```

### 8.4 关键逻辑

- 创建会话后携带 `sessionId` 进入聊天页
- WS 断线：指数退避重连 + `GET messages` 补拉增量
- 发送：`clientMsgId` 幂等，先展示「发送中」，收到 `CHAT_ACK` 变「已发送」
- 图片：先 `upload(biz=cs)` 再 `CHAT_SEND msgType=IMAGE`

---

## 9. 网关与部署

### 9.1 网关路由（Nacos / gateway）

```json
{
  "id": "jeecg-fondia-support",
  "predicates": [{
    "name": "Path",
    "args": {
      "_genkey_0": "/support/**",
      "_genkey_1": "/sys/fd/cs/**"
    }
  }],
  "uri": "lb://jeecg-support"
},
{
  "id": "jeecg-fondia-support-ws",
  "predicates": [{
    "name": "Path",
    "args": { "_genkey_0": "/ws/cs/**" }
  }],
  "uri": "lb:ws://jeecg-support"
}
```

### 9.2 注意事项

- WebSocket 需关闭网关对 idle 连接的过早断开
- 多副本 support 服务 **必须** 配 Redis 广播
- 雪花 ID 序列化：Long → String（与现有 Fondia 规范一致）

---

## 10. 安全与风控

| 项 | 方案 |
|----|------|
| 鉴权 | App JWT / 后台 Shiro Token；WS 握手校验 |
| 越权 | 用户只能访问自己的 session；客服需 accept 后才能发消息 |
| 限流 | 单用户每分钟消息数上限（如 30 条） |
| 内容 | 敏感词过滤（可选）；图片走 upload 白名单 |
| 匿名 | FAQ 可匿名；人工会话必须登录 |

---

## 11. 实施分期

### Phase 1 — MVP（2~3 周）

1. 建表 + support 模块骨架
2. FAQ/快捷入口 REST + 管理 CRUD
3. 会话创建 + 消息 REST
4. WebSocket 文本消息双向
5. App 两页面对接
6. 管理端工作台（单客服）

### Phase 2 — 增强

- 图片消息、已读回执
- 排队提示、超时自动关闭
- 多客服 + 简单轮询分配
- 会话统计报表

### Phase 3 — 可选

- AI 前置问答（FAQ 语义匹配）
- 与「意见反馈」打通（转工单）
- 消息模板 / 快捷回复

---

## 12. 与现有能力的关系

| 模块 | 关系 |
|------|------|
| 意见反馈 | 异步、非实时；客服中可引导用户「提交反馈」留档 |
| Jeecg `/websocket/{userId}` | 系统通知专用，**不复用** |
| `/user/upload?biz=feedback` | 可新增 `biz=cs` 供聊天图片 |

---

## 13. 验收标准（MVP）

- [ ] App FAQ 从接口加载，可展开查看答案
- [ ] 点击「召唤人工客服」创建会话并进入聊天
- [ ] App 与后台工作台文本消息实时互通（≤2s 延迟）
- [ ] 断线重连后可拉取历史消息
- [ ] 后台可管理 FAQ、查看/接入/关闭会话
- [ ] 消息与 sessionId 在 DB 可追溯

---

## 14. 待确认事项

1. **微服务**：独立 `jeecg-support` 还是并入 `jeecg-user`？
2. **客服人力**：初期是否仅 1 个客服账号（管理员兼岗）？
3. **服务时间**：非工作时间是否仅展示 FAQ + 「留言」转意见反馈？
4. **Gateway WS**：当前环境 Nacos 是否已启用 websocket 路由模板？

---

*文档维护：Fondia 项目组 · 如有变更请同步更新 `gateway_fondia_routes.json` 与 `init_fondia_admin_menu.sql`*
