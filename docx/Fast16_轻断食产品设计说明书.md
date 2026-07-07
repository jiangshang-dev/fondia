# Fast16 轻断食小程序 · 产品设计说明书

> **产品代号**：Fast16（16+8 轻断食）  
> **参考**：[给女朋友写了个轻断食小程序](https://juejin.cn/post/7610624459038081050)  
> **三端仓库**：
> - 小程序：`jeecguniapp` → `/Users/xiaobai/Deveploer/workspace/company/lingxi/jeecguniapp`
> - 后端：`jeecg-boot` → `.../JeecgBoot/jeecg-boot`
> - 管理后台：`jeecgboot-vue3` → `.../JeecgBoot/jeecgboot-vue3`
> **分支策略**：本分支专做轻断食，可删除 Fondia 菜谱相关模块/页面，不影响其他分支  
> **版本**：V1.0

---

## 一、产品定位与铁则

### 1.1 一句话

**打开即用的轻断食计时器** + **可选云端同步** + **克制使用的 AI 断食小助手**。

### 1.2 三条铁则（来自参考文章，本分支必须遵守）

| 铁则 | 说明 |
|------|------|
| 无广告 / 无会员 / 无付费 | 小程序与管理端均不设计商业化阻断 |
| 选择性登录 | **本地计时优先**；登录后仅做记录同步与 AI 配额 |
| 只做核心 | 不做社区、排行榜、带货食谱、体重体脂强制填写 |

### 1.3 与 Fondia 的差异

| 维度 | Fondia 饭典 | Fast16 轻断食 |
|------|-------------|---------------|
| 核心 | 菜谱 + AI 厨伴 | 断食计时 + 进食窗口 |
| 客户端 | Flutter + 鸿蒙 | **UniApp 微信小程序** |
| 后端 | 7+ 微服务 | **1～2 个业务模块即可** |
| AI | 菜谱推荐 Tool | **断食问答 / 计划建议**（无菜谱卡片） |

---

## 二、要不要新建微服务？

### 结论（推荐）

| 阶段 | 方案 | 说明 |
|------|------|------|
| **MVP（本分支）** | ✅ 新建 **`jeecg-module-fasting`** 业务模块<br>❌ **不新建独立 Cloud 微服务** | 与 `jeecg-system-start` 或单独 `jeecg-fasting-start` **单体/双模块**启动即可 |
| 复用 | `jeecg-module-system` | 管理端登录、权限、菜单、**Airag AI** |
| 复用/精简 | `jeecg-module-user` | 微信登录、App 用户表（可裁剪为仅小程序用户） |
| **可删除** | cuisine / interaction / search / member / payment / support | 本分支从 `pom.xml` 移除，网关路由删 Fondia 段 |

### 为什么不单独拆 `jeecg-fasting-cloud-start`？

轻断食业务 **QPS 低、表少、无复杂域**，拆微服务只会增加 Nacos/Feign/网关维护成本。  
待 DAU > 5 万或需独立扩容时，再把 `jeecg-module-fasting` 打成独立 jar + cloud-start。

### 本分支推荐后端形态

```
jeecg-boot（fasting 分支精简后）
├── jeecg-boot-base-core
├── jeecg-module-system          # 管理端 + Airag
├── jeecg-module-user            # 微信登录（精简）
├── jeecg-module-fasting         # 【新建】断食全部业务
└── jeecg-module-system/jeecg-system-start   # 单体启动（MVP）
    或 jeecg-server-cloud/
        ├── jeecg-gateway-cloud-start
        ├── jeecg-system-cloud-start
        └── jeecg-fasting-cloud-start        # 二期可选
```

---

## 三、系统架构

```
┌─────────────────────────────────────────────────────────┐
│  UniApp 微信小程序 (jeecguniapp)                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ 计时首页  │ │ 设置     │ │ 历史记录  │ │ AI 助手  │   │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │
│       │ 本地 Pinia  │            │            │          │
│       │ Storage    │            │ 登录后同步  │ SSE      │
└───────┼────────────┼────────────┼────────────┼──────────┘
        │            │            │            │
        ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────┐
│  Spring Boot API  (/fasting/**  /user/**  /airag/**)    │
│  ┌─────────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ jeecg-module-   │  │ jeecg-module │  │ jeecg-     │ │
│  │ fasting         │  │ -user        │  │ module-    │ │
│  │ 计划/会话/统计   │  │ 微信登录     │  │ system AI  │ │
│  └────────┬────────┘  └──────┬───────┘  └─────┬──────┘ │
└───────────┼──────────────────┼────────────────┼────────┘
            ▼                  ▼                ▼
         MySQL              Redis           Airag/LLM
```

---

## 四、小程序功能设计（UniApp）

### 4.1 页面结构（替换现有 pages-work / 低代码演示）

**`pages.json` 建议仅保留：**

| 路径 | 页面 | 说明 |
|------|------|------|
| `pages/index/index` | **计时首页** | 打开即见：已断食时长、距离可进食、状态文案、结束断食 |
| `pages/settings/index` | **设置** | 计划、进食窗口、触感、语言、关于 |
| `pages/history/index` | **历史** | 近 7/30 天完成天数（静默，不弹成就） |
| `pages/ai/index` | **AI 助手** | 可选；未登录提示本地模式 |
| `pages/login/index` | **登录** | 微信一键登录（可选） |
| `pages/about/index` | **什么是 16+8** | 单页弹窗式说明 |

**TabBar（2 个即可）：**

- 计时（首页）
- 我的（设置 + 历史 + AI 入口）

### 4.2 计时首页交互

```
状态机：IDLE → FASTING → EATING → FASTING …

┌─────────────────────────────┐
│      已断食  12:34:05         │  ← 大字号，每秒刷新
│   还有 3 小时 25 分可以进食    │
│   当前计划：16+8              │
│                             │
│   [ 结束断食 / 开始进食 ]     │
└─────────────────────────────┘
```

- **本地优先**：`pinia + uni.setStorageSync` 存当前会话、计划、进食窗口  
- **离线可用**：断网仍计时；联网后 `POST /fasting/session/sync` 合并  

### 4.3 设置项（仅 4 类）

| 配置项 | 选项 |
|--------|------|
| 断食计划 | 14:10 / **16:8** / 18:6 / 20:4 |
| 进食窗口 | 开始时间（如 12:00）→ 自动算结束 |
| 触感反馈 | 开/关 |
| 语言 | 中文 / English |

### 4.4 UniApp 目录建议（jeecguniapp）

```
src/
├── pages/
│   ├── index/           # 计时首页
│   ├── settings/
│   ├── history/
│   ├── ai/
│   └── login/
├── store/
│   └── fasting.ts       # 计时状态、计划、同步队列
├── api/
│   ├── fasting.ts
│   ├── user.ts
│   └── ai.ts
├── utils/
│   ├── timer.ts         # 断食/进食时长计算
│   └── wxLogin.ts
└── components/
    ├── FastingClock.vue # 大时钟
    └── PlanPicker.vue
```

### 4.5 可删除的 UniApp 内容（本分支）

- `pages-work/**`（低代码工作台演示）
- `components/online/**`（Online 表单）
- 与 Fondia/Jeecg 演示无关的 echarts 地图等  
- 保留：`utils/request`、登录封装、wot-design-uni、pinia

---

## 五、后端模块设计（jeecg-module-fasting）

### 5.1 包结构

```
jeecg-module-fasting/
├── jeecg-module-fasting-api/     # Feign/DTO（二期微服务用）
└── jeecg-module-fasting-biz/
    ├── controller/app/           # 小程序 API
    │   ├── FastingPlanController
    │   ├── FastingSessionController
    │   ├── FastingStatsController
    │   └── FastingAiController
    ├── controller/admin/         # 管理端 API
    ├── entity/
    ├── service/
    ├── mapper/
    └── resources/db/sql/
        └── init_fasting_tables.sql
```

### 5.2 数据库表

#### `fd_fast_plan` 断食计划模板（系统预置 + 后台可改）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK |
| code | varchar | `16_8` / `14_10` / `18_6` / `20_4` |
| name | varchar | 展示名 |
| fast_hours | int | 断食小时数 |
| eat_hours | int | 进食小时数 |
| description | varchar | 简短说明 |
| sort_order | int | 排序 |
| status | tinyint | 1 启用 |

#### `fd_fast_user_setting` 用户设置

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK |
| user_id | bigint | 可空（匿名设备用 device_id） |
| device_id | varchar | 未登录设备标识 |
| plan_code | varchar | 当前计划 |
| eat_start_time | time | 进食开始 如 12:00 |
| haptic_enabled | tinyint | 触感 |
| locale | varchar | zh_CN / en_US |
| update_time | datetime | |

#### `fd_fast_session` 断食/进食会话记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK |
| user_id | bigint | 可空 |
| device_id | varchar | |
| plan_code | varchar | |
| phase | varchar | `FASTING` / `EATING` |
| start_time | datetime | 阶段开始 |
| end_time | datetime | 阶段结束（进行中为空） |
| duration_sec | int | 实际时长 |
| client_uuid | varchar | 客户端幂等键，防重复同步 |
| create_time | datetime | |

#### `fd_fast_daily_stat` 日统计（可选，也可 SQL 聚合）

| 字段 | 类型 | 说明 |
|------|------|------|
| stat_date | date | |
| user_id | bigint | |
| plan_code | varchar | |
| target_fast_sec | int | 目标断食秒数 |
| actual_fast_sec | int | 实际 |
| completed | tinyint | 是否达标 |

#### `fd_fast_ai_message` AI 对话（轻量）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | |
| user_id | bigint | |
| session_id | varchar | |
| role | varchar | user/assistant |
| content | text | |
| create_time | datetime | |

### 5.3 App API 清单

**前缀：`/fasting/app`（网关可统一 `/fasting/**`）**

| 方法 | 路径 | 说明 | 登录 |
|------|------|------|------|
| GET | `/plans` | 计划列表 | 否 |
| GET | `/setting` | 获取用户/设备设置 | 可选 |
| PUT | `/setting` | 保存设置 | 可选 |
| POST | `/session/start` | 开始断食/进食 | 可选 |
| POST | `/session/end` | 结束当前阶段 | 可选 |
| POST | `/session/sync` | 批量同步本地记录 | 可选 |
| GET | `/session/current` | 当前进行中会话 | 可选 |
| GET | `/stats/weekly` | 近 7 天统计 | 可选 |
| POST | `/ai/chat` | AI 问答（非流式 MVP） | 是 |
| POST | `/ai/chat/stream` | AI 流式（二期） | 是 |

**用户登录（复用 user 模块，新增微信小程序）：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/auth/wxMiniLogin` | code 换 openId + JWT |

### 5.4 核心业务逻辑

**计时不必每秒调后端**——与参考文章一致，客户端本地计时，仅在：

- 开始/结束阶段  
- 切换计划  
- 登录后同步历史  

时调用 API。

**同步策略：**

1. 本地队列 `{ client_uuid, action, payload, ts }`  
2. 登录或 App 启动联网 → `POST /session/sync`  
3. 服务端按 `client_uuid` 幂等插入  

---

## 六、AI 功能设计（克制版）

### 6.1 原则

- **不**做「AI 生成食谱」「AI 减肥计划售卖」  
- **只做**断食相关短问答：能喝什么、头晕怎么办、计划怎么选、鼓励语  
- 回复控制在 **200 字内**，无 Markdown 大段  

### 6.2 实现方式（复用 Jeecg Airag）

| 项 | 方案 |
|----|------|
| 引擎 | `jeecg-module-system` 已有 Airag |
| 入口 | `FastingAiController` → 内部调 `AiragService` / 现有 SSE 封装 |
| 提示词 | 后台可配置 `fd_fast_ai_prompt` 表或 Nacos |
| 上下文 | 传入：当前计划、已断食时长、阶段（断食/进食） |
| 限流 | 未登录：每日 3 次（device_id）；登录：每日 20 次 |

**系统提示词示例：**

```
你是 Fast16 轻断食助手。只回答与间歇性断食相关的问题。
当前用户计划：{plan}，状态：{phase}，已持续：{duration}。
不要推荐药品、不要诊断疾病，不适建议就医。
回答简洁，不超过 200 字。
```

### 6.3 小程序 AI 页

- 3 个快捷问题 chips：`断食能喝咖啡吗` / `16+8 怎么选` / `头晕正常吗`  
- 输入框 + 发送  
- MVP 用普通 POST；二期改 SSE 流式  

---

## 七、管理后台设计（jeecgboot-vue3）

### 7.1 可删除内容（本分支）

```
src/views/fondia/          # 全部删除
src/views/dashboard/.../IndexFondia.vue 等 Fondia 看板
路由中 fondia 相关 menu
```

### 7.2 新建菜单结构

```
Fast16 轻断食
├── 数据看板          /fasting/dashboard
├── 断食计划管理      /fasting/plan
├── 用户设置查询      /fasting/user-setting
├── 会话记录          /fasting/session
├── 日统计报表        /fasting/daily-stat
├── AI 配置
│   ├── 提示词管理    /fasting/ai/prompt
│   └── 对话记录      /fasting/ai/message
└── 小程序用户        /fasting/user        # 精简用户列表
```

### 7.3 各页面职责

| 页面 | 功能 |
|------|------|
| **数据看板** | 今日活跃、进行中会话数、7 日达标率、AI 调用量 |
| **断食计划** | CRUD 14:10/16:8… 文案、排序、启用 |
| **用户设置** | 查 plan、eating 窗口，运营排查 |
| **会话记录** | 按 user/device/时间筛选，异常时长标记 |
| **日统计** | 导出 CSV，周达标率 |
| **AI 提示词** | 编辑 system prompt、快捷问题列表 |
| **AI 对话记录** | 审计、敏感词复查 |
| **小程序用户** | openId、昵称、注册时间、最后同步 |

### 7.4 前端目录

```
src/views/fasting/
├── dashboard/
│   ├── index.vue
│   └── fasting.api.ts
├── plan/
│   ├── index.vue
│   ├── plan.data.ts
│   └── components/PlanModal.vue
├── session/
├── dailyStat/
├── ai/
│   ├── prompt/
│   └── message/
└── user/
```

### 7.5 登录页

- 替换 `FondiaLoginBrand` → `Fast16LoginBrand`（绿色简约）  
- 系统名：**Fast16 轻断食管理端**

---

## 八、网关与权限（若保留 Gateway）

**`gateway_fasting_routes.json`（新建，替换 fondia 路由）：**

```json
[
  { "id": "fasting-app", "predicates": [{ "name": "Path", "args": { "_genkey_0": "/fasting/**" }}], "uri": "lb://jeecg-system" },
  { "id": "user-app", "predicates": [{ "name": "Path", "args": { "_genkey_0": "/user/**" }}], "uri": "lb://jeecg-system" },
  { "id": "fasting-admin", "predicates": [{ "name": "Path", "args": { "_genkey_0": "/sys/fd/fast/**" }}], "uri": "lb://jeecg-system" }
]
```

MVP 单体启动时可不走 Gateway，小程序直连 `http://localhost:8080/jeecg-boot`。

---

## 九、分支清理 Checklist

### jeecg-boot

- [ ] `pom.xml` 移除 module：cuisine、interaction、search、member、payment、support  
- [ ] 删除 `jeecg-server-cloud/*-cloud-start` 中已移除模块的启动器  
- [ ] 新建 `jeecg-module-fasting` + 加入 system-start 依赖  
- [ ] `db/init_fasting_tables.sql` + 菜单 SQL  
- [ ] 删除 `db/gateway_fondia_routes.json` → 换 `gateway_fasting_routes.json`  

### jeecgboot-vue3

- [ ] 删除 `src/views/fondia/**`  
- [ ] 新建 `src/views/fasting/**`  
- [ ] 修改 `src/router/routes` 与后台菜单 SQL  
- [ ] 登录 branding 改为 Fast16  

### jeecguniapp

- [ ] 精简 `pages.json`，删除 pages-work  
- [ ] 新建 fasting 页面与 store  
- [ ] `manifest.json` 微信小程序 appid  
- [ ] `.env` 指向 fasting 后端地址  

---

## 十、实施分期

| 期 | 范围 | 工期参考 |
|----|------|----------|
| **P0** | 本地计时 + 设置 + 计划切换（纯前端，无后端） | 3 天 |
| **P1** | `jeecg-module-fasting` 会话同步 + 微信登录 | 5 天 |
| **P2** | 管理后台计划/会话/看板 | 4 天 |
| **P3** | AI 助手 + 提示词后台 | 3 天 |
| **P4** | 历史统计、导出、上线审核 | 2 天 |

---

## 十一、微信小程序上线要点

- 类目：**工具 → 健康管理** 或 **生活服务**（以微信审核为准）  
- 隐私协议：说明可选登录、本地存储、同步与 AI 调用  
- 不申请通讯录等无关权限  
- 名称示例：**轻断食16+8** / **Fast16**  

---

## 十二、与参考文章的对照

| 参考文章能力 | 本设计 |
|--------------|--------|
| 打开即计时 | ✅ 首页无开屏无引导 |
| 16+8 等计划 | ✅ fd_fast_plan + 设置页 |
| 进食窗口 | ✅ eat_start_time 自动算结束 |
| 无广告会员 | ✅ 产品铁则 |
| 选择性登录 | ✅ 本地优先 + sync |
| 静默记录 | ✅ 历史页不弹成就 |
| — | ➕ AI 助手（克制，可关） |
| — | ➕ Jeecg 管理端运营 |

---

**下一步建议**：先在 `jeecg-boot` 执行 P0/P1——新建 `jeecg-module-fasting` 与 `init_fasting_tables.sql`，同时在 `jeecguniapp` 实现本地计时首页。需要我直接在三个仓库开始 scaffold（建模块 + 首页 + 删 fondia 页面）时说明即可。
