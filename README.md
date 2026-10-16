<p align="center">
  <a href="https://github.com/jiangshang-dev/fondia">
    <img src="https://img.shields.io/badge/Fondia-食养智能菜谱-ff6b35?style=for-the-badge" alt="Fondia">
  </a>
</p>

<p align="center">
  <a href="https://github.com/jiangshang-dev/fondia/stargazers">
    <img src="https://img.shields.io/github/stars/jiangshang-dev/fondia?style=social" alt="GitHub Stars">
  </a>
  <a href="https://github.com/jiangshang-dev/fondia/blob/fondia/LICENSE">
    <img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="License">
  </a>
  <img src="https://img.shields.io/badge/JDK-21-orange.svg" alt="JDK 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JeecgBoot-3.9.2-1890ff.svg" alt="JeecgBoot">
</p>

<h1 align="center">Fondia 饭典</h1>

<p align="center">
  食养智能菜谱平台后端 · 基于 <a href="https://github.com/jeecgboot/JeecgBoot">JeecgBoot</a> 的 Spring Cloud 微服务
</p>

---

## 先 Star，再 Clone

这个仓库对 **已经 Star 的人** 开放学习和二次开发。

还没点 Star，请不要 Clone，也不要 Fork 之后只拉代码不回访。

1. 打开 [jiangshang-dev/fondia](https://github.com/jiangshang-dev/fondia)。
2. 点右上角 **Star**。
3. Star 成功之后，再执行下面的命令。

```bash
git clone https://github.com/jiangshang-dev/fondia.git
cd fondia
```

Star 是这个项目继续维护文档、示例和问题回复的前提。未 Star 就 Clone，视为没有接受本仓库的使用约定。

---

## 这是什么

Fondia（饭典）是一套「菜谱内容 + AI 厨伴 + 会员权益」的后端。它在 JeecgBoot 3.9.2 上拆出用户、菜谱、搜索、互动、客服 AI、会员和支付等独立服务，由 Spring Cloud Gateway 对外提供统一入口。

适合用来学习：

- JeecgBoot 从单体切到 Spring Cloud 的工程组织
- 内容社区类 App 的微服务边界
- SSE 流式 AI、Elasticsearch 搜索、会员配额和支付回调

客户端（Flutter、HarmonyOS）不在本仓库。本仓库是后端与管理端对接所需的服务端代码。

## 功能

| 能力 | 说明 |
|------|------|
| 用户 | 邮箱验证码、密码登录、华为账号登录、资料与反馈 |
| 菜谱 | 分类、标签、Banner、食材步骤、视频、管理端录入 |
| 搜索 | Elasticsearch 全文检索与联想 |
| 互动 | 收藏、点赞、评论、浏览历史 |
| AI 厨伴 | 会话、SSE 流式输出、按菜谱检索的工具调用 |
| 会员 | VIP 套餐、AI 每日配额、积分、签到、积分商城 |
| 支付 | 微信、支付宝、Apple IAP 预下单与回调履约 |
| 平台 | 管理后台、菜单权限、字典、定时任务、文件上传、低代码能力（随 JeecgBoot） |

## 架构

```text
Flutter / HarmonyOS / 管理端
            │
            ▼
     Gateway :9999
            │
   ┌────────┼─────────┬──────────┬─────────┬──────────┐
   ▼        ▼         ▼          ▼         ▼          ▼
 user    cuisine   search   interaction  support   member / payment
 :7002    :7004     :7007      :7003      :7006    :7008 / :7009
   │
   └── system :7001（管理端、Airag）
            │
   MySQL · Redis · Elasticsearch · Nacos · 对象存储
```

网关路由见 `db/gateway_fondia_routes.json`。App 接口按服务前缀进入，例如 `/user/**`、`/cuisine/**`、`/support/**`；管理端菜谱、用户、客服走 `/sys/fd/**`。

## 技术栈

| 层次 | 选型 |
|------|------|
| 语言与框架 | Java 21、Spring Boot 3.5.5、Spring Cloud 2025.0.0 |
| 平台 | JeecgBoot 3.9.2、MyBatis-Plus 3.5.12、Apache Shiro 2 + JWT |
| 微服务 | Nacos、Spring Cloud Gateway、OpenFeign |
| 数据 | MySQL 8、Redis、Elasticsearch |
| 任务与文档 | XXL-Job、Quartz、Knife4j |
| 可观测 | SkyWalking（`docker-compose.yml` 可选编排） |
| 协议 | Apache License 2.0 |

## 工程结构

```text
fondia
├── jeecg-boot-base-core          认证、通用能力、基础配置
├── jeecg-module-system           系统管理与单体启动入口
├── jeecg-boot-module             平台扩展（Airag、Demo 等）
├── jeecg-module-user             App 用户
├── jeecg-module-cuisine          菜谱
├── jeecg-module-search           搜索
├── jeecg-module-interaction      互动
├── jeecg-module-support          AI / 在线客服
├── jeecg-module-member           会员、积分、签到、商城
├── jeecg-module-payment          统一支付
├── jeecg-module-ai               AI 能力
├── jeecg-server-cloud            微服务启动模块、网关、Nacos
├── db                            库表、Nacos、网关路由
├── docker-compose.yml            本地微服务编排
└── docx                          需求、概要、详细设计与课程大纲
```

每个业务模块按 `api` / `biz`（以及需要独立进程的 `start`）拆分。微服务进程的启动类在 `jeecg-server-cloud/*-cloud-start`。

## 服务端口

| 服务 | 模块 | 端口 | 职责 |
|------|------|------|------|
| Gateway | `jeecg-cloud-gateway` | 9999 | 统一入口 |
| system | `jeecg-system-cloud-start` | 7001 | 管理后台、Airag |
| user | `jeecg-user-cloud-start` | 7002 | 注册登录、资料、反馈 |
| interaction | `jeecg-interaction-cloud-start` | 7003 | 收藏、点赞、评论、浏览 |
| cuisine | `jeecg-cuisine-cloud-start` | 7004 | 菜谱、分类、Banner |
| support | `jeecg-support-cloud-start` | 7006 | AI 会话、SSE |
| search | `jeecg-search-cloud-start` | 7007 | ES 搜索 |
| member | `jeecg-member-cloud-start` | 7008 | 会员、配额、积分、商城 |
| payment | `jeecg-payment-cloud-start` | 7009 | 支付预下单与回调 |
| Nacos | `jeecg-cloud-nacos` | 容器 8848，编排映射 18848 | 注册与配置 |
| 管理端页面 | `jeecg-server-cloud/web` | 3100 | 前端静态资源（需自行放入 `dist`） |

`jeecg-ai-cloud-start` 的配置端口同样是 7009。与支付服务同时启动前，先改掉其中一个端口。

## 环境要求

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis
- Nacos 2.x（微服务模式）
- Elasticsearch 8（搜索与 AI 菜谱检索）
- Docker、Docker Compose（走容器编排时）

把数据库、Redis、支付密钥和华为 OAuth 密钥放到本地配置或 Nacos，不要提交真实口令。`docker-compose.yml` 里的账号只适合本机演示，上线前必须更换。

## 快速开始

确认已经 Star 之后再继续。

### 1. 初始化数据库

创建业务库（默认名 `jeecg-boot`）和 Nacos 库，然后按顺序导入：

```text
db/tables_nacos.sql
db/jeecgboot-mysql-5.7.sql
db/init_member_tables.sql
db/init_member_seed_data.sql
db/init_ai_quota.sql
db/init_fondia_admin_menu.sql
db/init_fondia_ai_recipe_prompt.sql
```

其余 `db/init_*.sql`、`db/alter_*.sql` 按实际环境增量执行。菜单脚本导入后，重新登录管理端才会看到新菜单。

### 2. 单体模式

只跑管理端和平台能力时：

```bash
cd jeecg-module-system/jeecg-system-start
mvn spring-boot:run
```

入口类是 `org.jeecg.JeecgSystemApplication`，默认上下文路径 `/jeecg-boot`。开发配置在该模块的 `application-dev.yml`，先改好 MySQL 和 Redis。

### 3. 微服务模式

```bash
mvn clean package -P SpringCloud -DskipTests
```

`-P SpringCloud` 才会把 `jeecg-server-cloud` 编进构建。

启动顺序：MySQL、Redis、Elasticsearch → Nacos → system → 各业务服务 → Gateway。

把 `db/gateway_fondia_routes.json` 导入 Nacos 的 Gateway 路由配置，再访问 `http://localhost:9999`。

### 4. Docker Compose

编排文件复用已有 Docker 网络 `mynet`，以及本机已启动的 MySQL、Redis、Elasticsearch、RabbitMQ，不会新建这些基础容器。

```bash
# 网关、系统服务、Nacos、SkyWalking、管理端页面
docker compose up -d --build

# 再拉起 user、cuisine、interaction、support、ai
docker compose --profile full up -d --build
```

`full` 不包含 search、member、payment。这三个服务按上一节的微服务方式单独启动，或自行补到编排文件里。

| 地址 | 说明 |
|------|------|
| http://localhost:3100 | 管理端页面 |
| http://localhost:9999 | Gateway |
| http://localhost:18848/nacos | Nacos（本机 8848 被占用时使用该映射） |
| http://localhost:18080 | SkyWalking UI |

## 文档

| 文档 | 内容 |
|------|------|
| [docx/README.md](docx/README.md) | 设计文档索引 |
| [docx/Fondia_全栈实战课程大纲.md](docx/Fondia_全栈实战课程大纲.md) | 从需求到部署的学习路径 |
| `docx/Fondia_软件需求分析说明书.docx` | 功能、用例、接口需求 |
| `docx/Fondia_软件概要设计说明书.docx` | 架构、服务划分、部署 |
| `docx/Fondia_软件详细设计说明书.docx` | 类、接口、时序、库表 |
| [db/在线客服.md](db/在线客服.md) | 在线客服设计说明 |
| [CLAUDE.md](CLAUDE.md) | 工程约定与构建命令 |

## 参与贡献

1. 先 Star 本仓库。
2. Fork 之后从 `fondia` 拉分支，分支名带上改动意图。
3. 提交前在对应模块执行测试，说明影响的接口和库表。
4. Pull Request 写清动机、验证方式和不兼容变更。

问题请到 [Issues](https://github.com/jiangshang-dev/fondia/issues) 提交，附上服务名、接口、日志和复现步骤。

## 开源协议

本仓库以 [Apache License 2.0](LICENSE) 发布。基于 JeecgBoot 的部分，同时遵守 [JeecgBoot](https://github.com/jeecgboot/JeecgBoot) 上游协议。使用、修改和再分发时保留版权与许可声明。

## 致谢

- [JeecgBoot](https://github.com/jeecgboot/JeecgBoot)：低代码与微服务底座
- Spring、Nacos、MyBatis-Plus、Elasticsearch、SkyWalking

---

如果这份代码或文档帮到了你，回到仓库点一次 **Star**。还没 Star，就先不要 Clone。
