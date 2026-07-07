# Fondia 食养智能菜谱平台 · 全栈实战课程大纲

> **适用对象**：具备 Java / 移动端基础，希望从零完成「需求 → 架构 → 开发 → 部署 → 上架」完整闭环的学员  
> **项目三端**：JeecgBoot 微服务后端 · Flutter App（iOS/Android）· HarmonyOS App（ArkTS）  
> **代码仓库**：
> - 后端：`jeecg-boot`（`/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot`）
> - Flutter：`fondia_app`（`/Users/xiaobai/Deveploer/workspace/itheima/fondia_app`）
> - 鸿蒙：`fondia`（`/Users/xiaobai/DevEcoStudioProjects/fondia`）
> **文档版本**：V1.0  
> **建议总学时**：约 120～160 课时（理论 30% + 实操 70%）

---

## 课程目标

学完本课程，学员能够：

1. 独立完成中小型「内容 + AI + 会员」类 App 的需求分析与概要/详细设计文档编写  
2. 基于 JeecgBoot 搭建 Spring Cloud 微服务，完成用户、菜谱、搜索、互动、客服 AI、会员、支付等模块  
3. 使用 Flutter 开发 iOS/Android 双端 App，使用 HarmonyOS ArkTS 开发鸿蒙原生 App  
4. 集成华为账号登录、邮箱登录、SSE 流式 AI 对话、Elasticsearch 搜索、Redis 缓存等能力  
5. 完成 Docker/云服务器部署、域名 HTTPS、Nginx 网关、监控与日志  
6. 了解 App Store、Google Play、华为应用市场、软著与备案等上架流程  

---

## 模块总览

| 模块 | 名称 | 建议学时 | 产出物 |
|------|------|----------|--------|
| M1 | 需求与产品设计 | 8h | 需求说明书、原型、用例图 |
| M2 | 架构与技术选型 | 8h | 概要设计、接口清单、库表设计 |
| M3 | 后端微服务开发 | 40h | 7 大业务微服务 + 网关 |
| M4 | AI 与搜索能力 | 16h | 流式对话、Tool、ES 索引 |
| M5 | Flutter 客户端 | 24h | iOS/Android App |
| M6 | HarmonyOS 客户端 | 20h | 鸿蒙原生 App |
| M7 | 联调测试 | 8h | 测试用例、Bug 清单 |
| M8 | 部署运维 | 12h | 生产环境、CI/CD |
| M9 | 域名与安全 | 4h | HTTPS、证书、WAF |
| M10 | 上架与合规 | 8h | 商店包、软著、隐私政策 |

---

## M1 需求与产品设计（8 课时）

### 1.1 项目背景与竞品分析
- 食养/菜谱赛道用户痛点：找菜难、不会做、缺个性化推荐  
- 产品定位：**Fondia 饭典** — 智能菜谱 + AI 厨伴 + 会员权益  
- 用户角色：游客、注册用户、VIP 会员、运营管理员  

### 1.2 功能需求梳理
| 端 | 核心功能 |
|----|----------|
| C 端 App | 首页 Banner、分类、搜索、菜谱详情、收藏/点赞/评论、AI 专属厨伴、我的（积分/VIP/设置） |
| 登录 | 邮箱验证码、密码登录、华为账号登录（鸿蒙 LoginType.ID） |
| 管理后台 | 菜谱/分类/标签/Banner 管理、用户/反馈/数据看板、AI 客服配置、会员与配额 |
| 后端 | 微服务 API、网关路由、文件上传、支付回调 |

**实操**：阅读 `docx/Fondia_软件需求分析说明书.docx`，补充自己项目的差异化功能点  

### 1.3 非功能需求
- 性能：首页加载 < 2s，AI 首字响应 < 3s（SSE）  
- 安全：JWT、HTTPS、敏感配置外置、华为 OAuth Secret 仅服务端  
- 兼容：Flutter 3.x + iOS 12+ / Android 8+；HarmonyOS NEXT API 12+  
- 可扩展：微服务独立扩容、ES/Redis/MySQL 分离  

### 1.4 原型与交互
- 底部 Tab：首页 / 分类 / AI / 我的  
- 登录页 → 邮箱/华为登录 → Token 持久化  
- AI 页：流式 Markdown + 菜谱卡片跳转  

**实验**：画出 4 个主 Tab 线框图，标注与后端 API 对应关系  

---

## M2 架构与技术选型（8 课时）

### 2.1 总体架构
```
[Flutter App / HarmonyOS App]
         │ HTTPS
         ▼
   [Nginx / 域名] → [Spring Cloud Gateway :9999]
         │
    ┌────┴────┬─────────┬──────────┬─────────┬────────┐
    ▼         ▼         ▼          ▼         ▼        ▼
 jeecg-user jeecg-cuisine jeecg-search jeecg-interaction
 jeecg-support jeecg-member jeecg-payment jeecg-system(Airag)
         │
    MySQL / Redis / Elasticsearch / Nacos / MinIO
```

### 2.2 微服务划分（与代码模块一一对应）
| 服务 | 模块路径 | 职责 |
|------|----------|------|
| jeecg-user | `jeecg-module-user` | 注册登录、华为 OAuth、用户资料、反馈 |
| jeecg-cuisine | `jeecg-module-cuisine` | 菜谱、分类、Banner、食材步骤 |
| jeecg-search | `jeecg-module-search` | ES 全文/联想搜索 |
| jeecg-interaction | `jeecg-module-interaction` | 收藏、点赞、评论、浏览历史 |
| jeecg-support | `jeecg-module-support` | AI 会话、SSE 流式、Tool 调菜谱搜索 |
| jeecg-member | `jeecg-module-member` | VIP、AI 配额、积分 |
| jeecg-payment | `jeecg-module-payment` | 微信/支付宝/Apple IAP 预下单 |
| jeecg-system | `jeecg-module-system` | 管理后台、Airag AI 引擎 |

**实操**：阅读 `db/gateway_fondia_routes.json`，理解 `/user/**`、`/support/**` 等路由  

### 2.3 技术栈清单
| 层次 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3、JeecgBoot 3、MyBatis-Plus、Nacos、OpenFeign |
| 数据 | MySQL 8、Redis 7、Elasticsearch 8 |
| AI | Jeecg Airag、SSE、Function Calling（search_fondia_recipe） |
| Flutter | Dart 3、Dio、Provider/GetX、flutter_markdown、SSE 客户端 |
| 鸿蒙 | ArkTS、Account Kit、HttpClient、DevEco Studio 5.x |

### 2.4 数据库与接口设计
- 核心表：`fd_user`、`fd_recipe`、`fd_cs_session`、`fd_cs_message`、`fd_member_plan`  
- REST 规范：统一 `Result<T>`、Bearer Token、网关前缀  
- 文档：`docx/Fondia_软件概要设计说明书.docx`、`Fondia_软件详细设计说明书.docx`  

**实验**：根据 `jeecg-module-user-biz/.../init_user_tables.sql` 画出 ER 图  

---

## M3 后端微服务开发（40 课时）

### 3.1 工程搭建与环境（4h）
- JDK 17、Maven、IDEA、Nacos、MySQL、Redis、ES 本地/ Docker 启动  
- 导入 `jeecg-boot`，配置 `application-dev.yml`  
- 启动顺序：Nacos → Gateway → user/cuisine/... → system  

### 3.2 用户模块 jeecg-user（6h）
- 邮箱验证码登录：`LoginController` → `FdUserServiceImpl.emailLogin`  
- 华为登录：`POST /user/auth/huaweiLogin` + `HuaweiOAuthClient` 换 token  
- Token：`TokenManager` 对应后端 JWT 签发  
- **实验**：Postman 调通邮箱登录与 `GET /user/profile`  

### 3.3 菜谱模块 jeecg-cuisine（6h）
- 菜谱 CRUD、分类树、Banner、详情（步骤/食材/视频）  
- 管理端 `/sys/fd/recipe/**`  
- App 端 `/cuisine/**`  
- **实验**：后台录入「锅包肉」，App 分类页可见  

### 3.4 搜索模块 jeecg-search（4h）
- ES 索引 mapping、同步菜谱、关键词/联想  
- Flutter `api/search.dart`、鸿蒙 `SearchPage.ets` 对接  

### 3.5 互动模块 jeecg-interaction（4h）
- 收藏、点赞、评论、浏览历史  
- 登录态校验、Feign 调 user 服务  

### 3.6 客服 AI 模块 jeecg-support（8h）
- 会话：`FdCsSessionServiceImpl`  
- SSE 流式：`CsAiServiceImpl` → Airag  
- Tool：`SearchTool` 调 search 服务返回菜谱卡片  
- 历史消息 `extraJson` 存 recipes，解决卡片丢失  
- **实验**：提问「推荐一道东北菜」，流式输出 + 可点击菜谱卡片  

### 3.7 会员与支付（6h）
- `jeecg-member`：VIP 套餐、AI 每日配额  
- `jeecg-payment`：预下单、回调、Feign 履约  
- Flutter `VipPay.dart`、配额展示  

### 3.8 网关与 Feign（2h）
- `gateway_fondia_routes.json` 导入 Nacos  
- 跨服务鉴权、Support 转发 Authorization 到 Airag  

---

## M4 AI 与搜索能力深化（16 课时）

### 4.1 Airag 与提示词
- 系统提示词 SQL：`db/init_fondia_ai_recipe_prompt.sql`  
- 约束 AI 输出 `[[recipe:菜名:id]]` 格式  

### 4.2 SSE 流式协议
- 事件类型：`MESSAGE`、`APP_LINK`、`APP_META`、`end`  
- Flutter：`lib/utils/sse_client.dart`、`AiExclusive.dart`  
- 防回退：`peakText`、过滤 `::jeecgToolExec::`  

### 4.3 Tool Calling
- `SearchTool.java` → `jeecg-search`  
- 流结束推送 `APP_LINK`（`fondia://recipe/{id}`）再 `APP_META`  

### 4.4 向量检索（扩展）
- Redis Vector Store、文档 RAG（可参考 ruiyada 项目）  
- 与本项目 ES 搜索的适用场景对比  

**实验**：修复「菜名当 ID」的 `NumberFormatException`（`AiRecipeMatchServiceImpl`）  

---

## M5 Flutter 客户端开发（24 课时）

### 5.1 工程结构（2h）
```
lib/
├── main.dart / app.dart
├── pages/          # 页面
├── components/     # 组件
├── api/            # 接口层
├── stores/         # TokenManager、LocaleManager
├── utils/          # DioRequest、SSE
└── routes/         # 路由
```

### 5.2 网络与鉴权（4h）
- `DioRequest.dart`：BaseURL、拦截器、Token 注入  
- `TokenManager.dart`：SharedPreferences 持久化  
- 环境切换：`constants/index.dart`  

### 5.3 首页与分类（4h）
- `Home/index.dart`：Banner、推荐  
- `Category/index.dart`、`CategoryDetail/DishDetail.dart`  

### 5.4 登录体系（4h）
- 邮箱登录、密码登录、忘记密码  
- `ProtocolAgreementRow` 协议勾选  

### 5.5 AI 专属厨伴（6h）
- `AiExclusive.dart`：SSE 流式、Markdown、菜谱卡片  
- 历史：`AiChatHistory.dart`  
- 配额：`ai_quota_helper.dart`  

### 5.6 我的与会员（4h）
- 个人资料、设置、VIP、积分、反馈  
- 多语言：`l10n/app_localizations.dart`  

**实验**：真机调试 iOS/Android，完成登录 → AI 对话 → 点击卡片进详情  

---

## M6 HarmonyOS 客户端开发（20 课时）

### 6.1 工程与 DevEco（2h）
- `entry/src/main/ets/` 目录结构  
- `module.json5`：bundleName、client_id、权限 INTERNET  

### 6.2 网络层（3h）
- `HttpClient.ets`、`HttpConstants.ets`  
- `TokenManager.ets`、`GlobalConstants.BASE_URL`  

### 6.3 华为账号登录（5h）
- 个人开发者：**LoginType.ID**（非一键登录）  
- `HuaweiAccountLoginSection.ets` + `HuaweiAuthService.fetchUserProfile`  
- 后端 `thirdparty.huawei.client-id` 与 `module.json5` 一致  
- AGC：SHA256 指纹、Client Secret  
- **实验**：真机华为登录 → 我的页显示昵称头像  

### 6.4 核心页面（6h）
- `MainPage.ets`：Tab 导航  
- `HomePage`、`CategoryPage`、`SearchPage`  
- `DishDetailPage`、`AiPage`  
- `MinePage`、`SettingPage`（退出登录在设置内）  

### 6.5 与 Flutter 差异（2h）
- ArkTS 严格模式：禁止无类型 `{}`  
- 路由 `RouterUtil.ets` vs Flutter `routes`  
- 双端 API 契约保持一致  

### 6.6 静默登录（扩展 2h）
- `HuaweiAuthService.silentLogin`  
- 可选挂到 `EntryAbility`  

**实验**：鸿蒙模拟器 + 真机各完成一遍主流程  

---

## M7 联调测试（8 课时）

### 7.1 接口联调清单
| 场景 | 端 | 接口 |
|------|-----|------|
| 登录 | Flutter/鸿蒙 | POST `/user/auth/emailLogin` |
| 华为登录 | 鸿蒙 | POST `/user/auth/huaweiLogin` |
| 搜索 | 双端 | GET `/search/recipe` |
| AI 流式 | 双端 | POST `/support/ai/chat/stream` |
| VIP | Flutter | `/member/**`、`/payment/**` |

### 7.2 常见问题排错
- 502：网关路由/Nacos 服务未注册  
- 401：Token 过期、网关未转发 Authorization  
- AI 卡片丢失：执行 `db/alter_fd_cs_message_extra_json.sql`  
- 华为 `client_id not match`：Nacos 与 App metadata 不一致  

### 7.3 测试类型
- 单元测试：Service 层关键方法  
- 接口测试：Postman / Apifox 集合  
- UI 测试：主路径手工 + 可选 Flutter integration_test  

---

## M8 部署运维（12 课时）

### 8.1 服务器选型
- 入门：**4C8G** 单机 Docker（Redis + ES + 微服务同机，MySQL 用云 RDS）  
- 生产：应用与 ES/Redis 分离，RDS 高可用按需  

### 8.2 Docker Compose 部署
- MySQL、Redis、ES、Nacos、MinIO  
- 各 `*-cloud-start` 打 jar 镜像  
- 环境变量：`SPRING_PROFILES_ACTIVE=prod`  

### 8.3 Nginx 反向代理
- `api.fondia.com` → Gateway:9999  
- WebSocket/SSE：`proxy_buffering off`、`proxy_read_timeout`  
- 静态资源 / OSS 分离  

### 8.4 配置外置
- Nacos 配置中心：`thirdparty.huawei.*`、数据库、Redis  
- 敏感项：Client Secret、支付密钥不入 Git  

### 8.5 日志与监控
- Logback 按天滚动  
- 可选：Prometheus + Grafana、SkyWalking  

**实验**：在云服务器 Docker 跑通 Gateway + user + support，手机 App 改 BASE_URL 访问  

---

## M9 域名与安全（4 课时）

### 9.1 域名与备案
- 域名购买、ICP 备案（国内服务器必须）  
- 公安备案（按需）  

### 9.2 HTTPS
- Let's Encrypt 或云厂商免费证书  
- Nginx `ssl_certificate` 配置  
- App 强制 HTTPS，Android `networkSecurityConfig`、鸿蒙 cleartext 生产关闭  

### 9.3 API 域名规划示例
| 域名 | 用途 |
|------|------|
| `api.fondia.com` | 移动端 API |
| `admin.fondia.com` | 管理后台 |
| `static.fondia.com` | CDN/OSS 图片 |

### 9.4 安全加固
- 网关限流、CORS 白名单  
- Shiro/JWT 过期策略  
- 隐私数据加密存储  

---

## M10 上架应用市场与合规（8 课时）

### 10.1 软件著作权
- 材料：`docx/generate_copyright_materials.py` 生成程序/文档鉴别 PDF  
- 设计文档：`Fondia_软件需求分析说明书.docx` 等  

### 10.2 隐私与协议
- 《用户协议》《隐私政策》  
- Flutter `LegalContent.dart`、鸿蒙 `LegalContent.ets`  
- 隐私清单：华为/苹果/Google 各平台模板  

### 10.3 华为应用市场（HarmonyOS）
- 开发者实名（个人/企业）  
- AGC 创建应用、包名 `com.xiaobai.fondia`  
- Account Kit：个人用 LoginType.ID，企业可申请一键登录  
- 上传 HAP/APP、测试版 → 正式版  
- 图标、截图、分级、内容审核  

### 10.4 Apple App Store（Flutter iOS）
- Apple Developer 账号（$99/年）  
- 证书、Provisioning Profile、Archive 上传  
- App Store Connect：元数据、审核、IAP 配置  
- 隐私营养标签、ATT（若用追踪）  

### 10.5 Google Play（Flutter Android）
- Google Play Console（$25 一次性）  
- AAB 签名、targetSdk 要求  
- 数据安全表单、内容分级  

### 10.6 国内 Android 渠道（可选）
- 小米、OPPO、vivo、应用宝等开发者平台  
- 软著、备案号展示  

### 10.7 上架检查清单
- [ ] 生产 API 域名 HTTPS  
- [ ] 无测试账号/密码硬编码  
- [ ] 崩溃率、ANR 可控  
- [ ] AI 生成内容合规说明  
- [ ] 支付资质与 IAP 一致  
- [ ] 华为 client_id / 签名指纹与线上一致  

---

## 附录 A：推荐学习顺序（16 周示例）

| 周次 | 内容 |
|------|------|
| 1～2 | M1 需求 + M2 架构，输出设计文档 |
| 3～6 | M3 后端各微服务逐个击破 |
| 7 | M4 AI/SSE/Tool |
| 8～10 | M5 Flutter 全页面 |
| 11～12 | M6 HarmonyOS + 华为登录 |
| 13 | M7 联调测试 |
| 14 | M8 部署上云 |
| 15 | M9 域名 HTTPS |
| 16 | M10 软著 + 商店上架 |

---

## 附录 B：关键文件索引

| 主题 | 路径 |
|------|------|
| 网关路由 | `db/gateway_fondia_routes.json` |
| 华为登录后端 | `jeecg-module-user/.../HuaweiOAuthClient.java` |
| AI 流式会话 | `jeecg-module-support/.../FdCsSessionServiceImpl.java` |
| Flutter AI | `fondia_app/lib/pages/Ai/AiExclusive.dart` |
| 鸿蒙华为登录 | `fondia/entry/.../HuaweiAccountLoginSection.ets` |
| 设计文档生成 | `docx/generate_fondia_docs.py` |
| 软著材料 | `docx/generate_copyright_materials.py` |
| AI 消息扩展 SQL | `db/alter_fd_cs_message_extra_json.sql` |

---

## 附录 C：考核方式建议

| 类型 | 占比 | 说明 |
|------|------|------|
| 阶段作业 | 40% | 每模块完成指定实验并提交截图/代码 |
| 综合项目 | 40% | 三端跑通「登录 → 搜菜 → AI 推荐 → 进详情」 |
| 答辩 | 20% | 架构图讲解 + 部署演示 + 上架计划 |

---

**编制说明**：本大纲基于 Fondia 项目当前真实代码结构编写，随项目迭代可在 `docx/` 目录更新版本号。如需 Word 版，可使用 `generate_fondia_docs.py` 同款 python-docx 模板扩展生成。
