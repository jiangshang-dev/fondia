#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Fondia 食养 App 项目文档生成器
生成：需求分析、概要设计、详细设计 三份 Word 文档（各约 100 页）
依赖：pip install python-docx
"""

from __future__ import annotations

import os
from datetime import date
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.shared import Pt, Cm, RGBColor

OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT = "Fondia 食养智能菜谱平台"
VERSION = "V1.0"
TODAY = date.today().strftime("%Y-%m-%d")
TARGET_PAGES = 100

MODULES = [
    {
        "id": "user",
        "name": "用户中心模块",
        "service": "jeecg-user (7002)",
        "desc": "负责 App 用户注册登录、账号安全、第三方授权、个人信息与注销等能力。",
        "features": [
            "邮箱/密码登录与注册",
            "手机号验证码登录",
            "华为账号一键登录",
            "忘记密码与重置密码",
            "账号注销与数据预览",
            "用户资料编辑（昵称、头像、性别）",
            "Token 鉴权与刷新",
            "管理端用户列表与会员状态展示",
        ],
    },
    {
        "id": "cuisine",
        "name": "菜谱内容模块",
        "service": "jeecg-cuisine (7003)",
        "desc": "负责菜谱、分类、标签、Banner 等内容的生产、审核与 App 端展示。",
        "features": [
            "菜谱 CRUD 与上下架",
            "菜谱分类树管理",
            "菜谱标签管理",
            "Banner 轮播配置",
            "菜谱详情（食材、步骤、营养信息）",
            "封面图与多媒体资源",
            "ES 索引同步触发（RabbitMQ）",
            "管理端批量导入导出",
        ],
    },
    {
        "id": "interaction",
        "name": "用户互动模块",
        "service": "jeecg-interaction (7004)",
        "desc": "负责评论、点赞、收藏、浏览历史等用户与内容的互动行为。",
        "features": [
            "菜谱评论与回复",
            "点赞/取消点赞",
            "收藏/取消收藏",
            "浏览历史记录",
            "我的评论列表",
            "评论审核与敏感词过滤",
            "互动数据统计",
        ],
    },
    {
        "id": "search",
        "name": "搜索服务模块",
        "service": "jeecg-search (7007)",
        "desc": "基于 Elasticsearch 的菜谱全文检索与 AI 菜谱匹配搜索。",
        "features": [
            "关键词搜索与分页",
            "分类筛选与排序",
            "热搜词管理",
            "索引增量同步（RabbitMQ 消费）",
            "全量索引重建",
            "AI 菜谱名称模糊匹配",
            "搜索 Feign 对内接口",
        ],
    },
    {
        "id": "support",
        "name": "客服与 AI 助手模块",
        "service": "jeecg-support (7006)",
        "desc": "提供 AI 营养咨询、对话记忆、菜谱 Tool 调用、人工客服与 FAQ。",
        "features": [
            "AI 助手 SSE 流式对话",
            "会话创建与历史记录",
            "Airag 大模型接入与记忆",
            "search_fondia_recipe Tool 调用",
            "菜谱卡片解析与推荐",
            "转人工客服",
            "FAQ 与快捷入口",
            "管理端客服配置与会话监控",
        ],
    },
    {
        "id": "member",
        "name": "会员与积分模块",
        "service": "jeecg-member (7008)",
        "desc": "负责 VIP 会员订阅、积分任务、积分商城、AI 使用配额等商业化能力。",
        "features": [
            "会员套餐与订阅",
            "会员权益（AI 次数、专属内容）",
            "AI 配额查询与扣减",
            "配额管理后台配置",
            "积分获取任务",
            "积分商城兑换",
            "收货地址管理",
            "会员到期提醒",
        ],
    },
    {
        "id": "payment",
        "name": "支付模块",
        "service": "jeecg-payment (7009)",
        "desc": "对接支付宝等支付渠道，完成会员订单创建、支付回调与履约。",
        "features": [
            "创建会员支付订单",
            "支付宝 App 支付",
            "支付结果回调验签",
            "订单状态机",
            "支付履约（开通会员）",
            "退款流程（预留）",
            "支付渠道配置",
        ],
    },
    {
        "id": "app",
        "name": "Flutter 移动客户端",
        "service": "fondia_app",
        "desc": "跨平台移动应用，覆盖 iOS、Android、HarmonyOS 及 Web 调试。",
        "features": [
            "首页与分类浏览",
            "菜谱详情与步骤",
            "AI 营养咨询表单",
            "AI 助手流式聊天",
            "我的中心与设置",
            "会员开通与支付",
            "登录注册全流程",
            "多语言与隐私设置",
        ],
    },
]

NFR_ITEMS = [
    ("性能", "网关 P99 响应 < 500ms；AI 首 token < 3s；搜索 P95 < 200ms。"),
    ("可用性", "核心服务可用性 ≥ 99.5%；支持单节点故障自动切换。"),
    ("安全", "全链路 HTTPS；JWT 鉴权；敏感数据加密存储；支付回调验签。"),
    ("扩展性", "微服务独立部署；ES 与 MySQL 读写分离预留；AI 模型可替换。"),
    ("兼容性", "Flutter 3.x；iOS 13+；Android 8+；HarmonyOS API 12+。"),
    ("可维护性", "统一日志规范；Swagger 接口文档；SQL 版本化管理。"),
    ("合规", "隐私政策与用户协议；账号注销合规；个人信息最小采集。"),
]


def setup_styles(doc: Document) -> None:
    style = doc.styles["Normal"]
    style.font.name = "宋体"
    style.font.size = Pt(12)
    style._element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    style.paragraph_format.line_spacing = 1.5
    style.paragraph_format.space_after = Pt(6)

    for level in range(1, 4):
        hs = doc.styles[f"Heading {level}"]
        hs.font.name = "黑体"
        hs._element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
        hs.font.color.rgb = RGBColor(0, 0, 0)
        hs.font.size = Pt(18 - level * 2)


def add_cover(doc: Document, title: str, subtitle: str) -> None:
    for _ in range(6):
        doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(title)
    run.bold = True
    run.font.size = Pt(26)
    run.font.name = "黑体"
    run._element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")

    doc.add_paragraph()
    p2 = doc.add_paragraph()
    p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r2 = p2.add_run(subtitle)
    r2.font.size = Pt(16)

    for _ in range(8):
        doc.add_paragraph()
    meta = doc.add_paragraph()
    meta.alignment = WD_ALIGN_PARAGRAPH.CENTER
    meta.add_run(f"项目名称：{PROJECT}\n")
    meta.add_run(f"文档版本：{VERSION}\n")
    meta.add_run(f"编制日期：{TODAY}\n")
    meta.add_run("编制单位：灵犀科技研发团队\n")
    doc.add_page_break()


def add_toc_placeholder(doc: Document) -> None:
    doc.add_heading("目  录", level=1)
    doc.add_paragraph(
        "（请在 Microsoft Word 中：引用 → 目录 → 自动目录，更新域即可生成正式目录。）"
    )
    doc.add_page_break()


def add_body_text(doc: Document, text: str) -> None:
    doc.add_paragraph(text)


def add_bullet_list(doc: Document, items: list[str]) -> None:
    for item in items:
        doc.add_paragraph(item, style="List Bullet")


def add_numbered_list(doc: Document, items: list[str]) -> None:
    for item in items:
        doc.add_paragraph(item, style="List Number")


def add_table(doc: Document, headers: list[str], rows: list[list[str]]) -> None:
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = "Table Grid"
    hdr = table.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = h
    for ri, row in enumerate(rows):
        for ci, val in enumerate(row):
            table.rows[ri + 1].cells[ci].text = val
    doc.add_paragraph()


def page_break(doc: Document) -> None:
    doc.add_page_break()


def add_rich_paragraphs(doc: Document, topic: str, count: int = 4) -> None:
    """补充多段说明文字，增加文档篇幅与可读性。"""
    templates = [
        f"在 {topic} 的实现过程中，需充分考虑高并发场景下的数据一致性与接口幂等性。"
        f"对于读多写少的业务，应优先采用缓存策略，并设置合理的 TTL 与主动失效机制，"
        f"避免脏读与缓存穿透。",
        f"从用户体验角度，{topic} 相关操作应提供明确的加载状态与错误反馈，"
        f"网络异常时支持重试，关键流程（如支付、扣次）需有防重复提交设计。",
        f"在测试策略上，{topic} 应覆盖单元测试（Service 层）、接口测试（Controller 层）"
        f"及端到端场景测试。性能测试需模拟峰值 QPS，验证数据库连接池与线程池配置是否合理。",
        f"运维监控方面，{topic} 需接入统一日志平台，关键指标包括接口耗时、错误率、"
        f"下游 Feign 调用成功率。告警阈值应区分 P0/P1 级别，确保故障可快速定位与恢复。",
        f"安全合规方面，{topic} 涉及的用户数据须遵循最小够用原则，日志中禁止输出明文密码、"
        f"Token 完整串及支付敏感字段。接口需防止 SQL 注入、XSS 与越权访问。",
        f"版本演进时，{topic} 的 API 变更须保持向后兼容或提供版本号路由；"
        f"数据库表结构变更通过 Flyway/Liquibase 或 SQL 脚本版本化管理，禁止手工改表。",
    ]
    for i in range(count):
        add_body_text(doc, templates[i % len(templates)])


def fill_to_pages(doc: Document, current_sections: int, doc_type: str) -> None:
    """补充章节直至约 TARGET_PAGES 页（每节约 1 页正文）。"""
    appendix_start = current_sections + 1
    needed = max(0, TARGET_PAGES - current_sections - 3)
    for i in range(needed):
        idx = appendix_start + i
        mod = MODULES[i % len(MODULES)]
        feat = mod["features"][i % len(mod["features"])]

        if doc_type == "req":
            doc.add_heading(f"附录 A-{idx:03d} 用例补充：{mod['name']} - {feat}", level=2)
            add_body_text(
                doc,
                f"用例编号：UC-{mod['id'].upper()}-{idx:03d}\n"
                f"用例名称：{feat}\n"
                f"参与者：App 用户、系统\n"
                f"前置条件：用户已登录且网络正常；相关微服务 {mod['service']} 可用。\n"
                f"后置条件：系统持久化业务数据并返回符合接口契约的结果。\n"
            )
            doc.add_heading("基本事件流", level=3)
            add_numbered_list(
                doc,
                [
                    f"用户在 App 端发起「{feat}」相关操作。",
                    "客户端携带 JWT Token 经网关路由至对应微服务。",
                    "服务端校验参数与权限，执行业务逻辑并访问数据库。",
                    "若涉及搜索或 AI，调用 jeecg-search 或 jeecg-system Airag 服务。",
                    "服务端返回统一 Result 结构或 SSE 事件流。",
                    "客户端解析响应并更新 UI，必要时展示错误提示。",
                ],
            )
            doc.add_heading("扩展事件流", level=3)
            add_bullet_list(
                doc,
                [
                    "E1：Token 过期 → 跳转登录页。",
                    "E2：参数校验失败 → 返回 400 与字段错误信息。",
                    "E3：下游服务超时 → 返回友好降级文案。",
                    "E4：并发冲突 → 乐观锁重试或提示用户刷新。",
                ],
            )
            doc.add_heading("业务规则", level=3)
            add_bullet_list(
                doc,
                [
                    "所有写操作需记录操作人与时间戳。",
                    "敏感字段（手机号、邮箱）脱敏展示。",
                    "与会员/配额相关的操作需先校验权益。",
                    "涉及 UGC 的内容需经过敏感词过滤。",
                ],
            )
            add_rich_paragraphs(doc, feat, count=5)

        elif doc_type == "hld":
            doc.add_heading(f"附录 B-{idx:03d} 模块设计补充：{mod['name']}", level=2)
            add_body_text(doc, mod["desc"])
            doc.add_heading("逻辑架构", level=3)
            add_body_text(
                doc,
                f"{mod['name']} 采用 Controller → Service → Mapper 三层结构，"
                f"对外通过 REST API 暴露能力，对内通过 OpenFeign 调用其他微服务。"
                f"部署单元为 {mod['service']}，注册至 Nacos，经 Spring Cloud Gateway 统一路由。"
            )
            doc.add_heading("关键设计决策", level=3)
            add_bullet_list(
                doc,
                [
                    f"功能点「{feat}」独立 Service 方法实现，便于单元测试。",
                    "数据库事务边界控制在单服务内，跨服务采用最终一致性。",
                    "缓存热点读数据，写操作主动失效缓存。",
                    "异步任务使用专用线程池，避免阻塞 Servlet 线程。",
                ],
            )
            doc.add_heading("与其他模块协作", level=3)
            add_body_text(
                doc,
                "协作关系通过 Feign 接口定义在 *-api 子模块，"
                "消费方仅依赖 API 模块而非 BIZ 实现，降低耦合。"
                "事件驱动场景（如菜谱变更同步 ES）通过 RabbitMQ 解耦。"
            )
            add_rich_paragraphs(doc, mod["name"], count=5)

        else:  # detail
            doc.add_heading(f"附录 C-{idx:03d} 详细设计：{mod['name']} - {feat}", level=2)
            doc.add_heading("类与接口", level=3)
            add_body_text(
                doc,
                f"包路径：org.jeecg.modules.{mod['id']}\n"
                f"Controller 层负责参数校验与 Result 封装；\n"
                f"Service 层实现 {feat} 的核心业务规则；\n"
                f"Mapper 层基于 MyBatis-Plus 访问 fd_* 表。"
            )
            doc.add_heading("接口定义（示例）", level=3)
            add_table(
                doc,
                ["方法", "路径", "说明"],
                [
                    ["GET", f"/{mod['id']}/example", "查询示例"],
                    ["POST", f"/{mod['id']}/example", "创建示例"],
                    ["PUT", f"/{mod['id']}/example/{{id}}", "更新示例"],
                    ["DELETE", f"/{mod['id']}/example/{{id}}", "删除示例"],
                ],
            )
            doc.add_heading("数据库字段（示例）", level=3)
            add_table(
                doc,
                ["字段", "类型", "说明"],
                [
                    ["id", "bigint", "主键"],
                    ["create_time", "datetime", "创建时间"],
                    ["update_time", "datetime", "更新时间"],
                    ["status", "tinyint", "状态 0-禁用 1-启用"],
                    ["remark", "varchar(500)", "备注"],
                ],
            )
            doc.add_heading("异常与错误码", level=3)
            add_table(
                doc,
                ["错误码", "HTTP", "说明"],
                [
                    ["400", "400", "参数校验失败"],
                    ["401", "401", "未登录或 Token 无效"],
                    ["403", "403", "无权限"],
                    ["500", "500", "服务器内部错误"],
                ],
            )
            doc.add_heading("时序说明", level=3)
            add_body_text(
                doc,
                f"1. Client → Gateway → {mod['service']}\n"
                f"2. Controller 调用 XxxService.{feat}()\n"
                f"3. Service 读写 MySQL / 调用 Feign / 发送 MQ\n"
                f"4. 返回 Result<T> 或 SSE EventData 流\n"
                f"5. Client 解析并渲染"
            )
            add_rich_paragraphs(doc, f"{mod['name']}-{feat}", count=5)

        if i < needed - 1:
            page_break(doc)


def build_requirements_doc() -> Document:
    doc = Document()
    setup_styles(doc)
    add_cover(doc, "Fondia 食养平台", "软件需求分析说明书")
    add_toc_placeholder(doc)

    doc.add_heading("1 引言", level=1)
    doc.add_heading("1.1 编写目的", level=2)
    add_body_text(
        doc,
        f"本文档是 {PROJECT} 的软件需求分析说明书（SRS），"
        "用于明确系统的功能需求、非功能需求、用户特征及运行环境，"
        "作为概要设计、详细设计、开发、测试与验收的依据。"
        "预期读者包括产品经理、架构师、开发人员、测试人员及项目管理人员。"
    )
    doc.add_heading("1.2 项目背景", level=2)
    add_body_text(
        doc,
        "随着国民健康意识提升，个性化营养与科学饮食需求快速增长。"
        "Fondia 食养平台面向关注健康饮食的用户，提供菜谱浏览、营养咨询、"
        "AI 智能推荐、会员增值服务等一站式能力。"
        "系统采用 JeecgBoot 微服务架构，包含 Flutter 移动端、Vue3 管理后台及多领域微服务。"
    )
    doc.add_heading("1.3 术语与缩略语", level=2)
    add_table(
        doc,
        ["术语", "说明"],
        [
            ["Airag", "JeecgBoot 内置 AI 大模型应用框架"],
            ["SSE", "Server-Sent Events，服务端推送流式事件"],
            ["ES", "Elasticsearch 全文检索引擎"],
            ["UGC", "User Generated Content，用户生成内容"],
            ["VIP", "付费会员用户"],
            ["Feign", "声明式 HTTP 客户端，用于微服务间调用"],
        ],
    )
    doc.add_heading("1.4 参考资料", level=2)
    add_bullet_list(
        doc,
        [
            "GB/T 8567-2006 计算机软件文档编制规范",
            "JeecgBoot 3.x 官方开发文档",
            "Flutter 3.x 官方文档",
            "项目 db/ 目录 SQL 初始化脚本",
            "gateway_fondia_routes.json 网关路由配置",
        ],
    )
    page_break(doc)

    doc.add_heading("2 项目概述", level=1)
    doc.add_heading("2.1 产品定位", level=2)
    add_body_text(
        doc,
        "Fondia 是一款以「科学食养」为核心的智能菜谱 App，"
        "通过 AI 营养助手帮助用户根据体质、目标（减脂、控糖等）获得个性化饮食建议，"
        "并推荐 App 内可操作的菜谱卡片，形成「咨询—推荐—烹饪」闭环。"
    )
    doc.add_heading("2.2 用户角色", level=2)
    add_table(
        doc,
        ["角色", "描述", "主要诉求"],
        [
            ["游客", "未登录用户", "浏览部分内容，引导注册"],
            ["普通用户", "已注册非会员", "免费使用有限 AI 次数，浏览菜谱"],
            ["VIP 会员", "付费订阅用户", "更高 AI 配额、专属权益"],
            ["运营人员", "管理后台用户", "内容运营、用户管理、数据看板"],
            ["客服人员", "管理端客服", "处理人工会话与 FAQ"],
            ["系统管理员", "超级管理员", "系统配置、权限、菜单管理"],
        ],
    )
    doc.add_heading("2.3 系统边界", level=2)
    add_body_text(
        doc,
        "系统边界包括：Flutter App、Vue3 管理后台、API 网关、"
        "user/cuisine/interaction/search/support/member/payment 微服务、"
        "MySQL、Redis、Elasticsearch、RabbitMQ、Nacos 及第三方（华为账号、支付宝、大模型 API）。"
        "不包含：食材电商配送、线下门店 POS、医疗诊断等超出食养咨询范围的能力。"
    )
    page_break(doc)

    doc.add_heading("3 功能需求", level=1)
    section_count = 3
    for mod in MODULES:
        doc.add_heading(f"3.{MODULES.index(mod)+1} {mod['name']}", level=2)
        add_body_text(doc, mod["desc"])
        doc.add_heading("功能列表", level=3)
        for j, feat in enumerate(mod["features"], 1):
            doc.add_heading(f"FR-{mod['id'].upper()}-{j:02d} {feat}", level=4)
            add_body_text(
                doc,
                f"需求描述：系统应支持{feat}。"
                f"该功能归属于{mod['name']}（{mod['service']}），"
                "需满足可用性、安全性及用户体验要求。"
            )
            add_body_text(doc, "输入：用户操作或系统触发事件。")
            add_body_text(doc, "处理：服务端执行业务规则并持久化。")
            add_body_text(doc, "输出：结构化 JSON 或 SSE 流式文本。")
            add_body_text(
                doc,
                "验收标准：功能可用；异常有提示；关键路径有日志；"
                "接口响应符合 Swagger 定义；核心场景通过测试用例。"
            )
        page_break(doc)
        section_count += 1

    doc.add_heading("4 非功能需求", level=1)
    for i, (cat, desc) in enumerate(NFR_ITEMS, 1):
        doc.add_heading(f"4.{i} {cat}", level=2)
        add_body_text(doc, desc)
    page_break(doc)

    doc.add_heading("5 数据需求", level=1)
    add_body_text(
        doc,
        "系统核心数据实体包括：用户(fd_user)、菜谱(fd_recipe)、分类(fd_recipe_category)、"
        "评论(fd_comment)、会话(fd_cs_session)、会员订单(fd_member_order)、"
        "AI 配额配置(fd_ai_quota_config)、积分账户(fd_points_account) 等。"
        "详细 ER 图见概要设计文档。"
    )
    doc.add_heading("6 接口需求", level=1)
    add_body_text(
        doc,
        "App 端统一经 Gateway (9999) 访问：/user/**、/cuisine/**、/interaction/**、"
        "/search/**、/support/**、/member/**、/payment/**。"
        "管理端路由：/sys/fd/**。AI 流式接口：POST /support/ai/chat，Content-Type: text/event-stream。"
    )
    doc.add_heading("7 运行环境", level=1)
    add_table(
        doc,
        ["类别", "要求"],
        [
            ["服务端 OS", "Linux CentOS 7+ / Ubuntu 20.04+"],
            ["JDK", "JDK 17"],
            ["数据库", "MySQL 8.0"],
            ["缓存", "Redis 6.x"],
            ["搜索", "Elasticsearch 7.x"],
            ["消息队列", "RabbitMQ 3.x"],
            ["注册中心", "Nacos 2.x"],
            ["客户端", "Flutter 3.x, Dart 3.x"],
        ],
    )

    fill_to_pages(doc, section_count + 4, "req")
    return doc


def build_hld_doc() -> Document:
    doc = Document()
    setup_styles(doc)
    add_cover(doc, "Fondia 食养平台", "软件概要设计说明书")
    add_toc_placeholder(doc)

    doc.add_heading("1 引言", level=1)
    doc.add_heading("1.1 编写目的", level=2)
    add_body_text(
        doc,
        "本文档描述 Fondia 食养平台的概要设计，包括系统架构、模块划分、"
        "接口设计、数据库概要、安全与部署方案，为详细设计与编码提供指导。"
    )
    doc.add_heading("1.2 与需求文档关系", level=2)
    add_body_text(
        doc,
        "概要设计以《软件需求分析说明书》为依据，"
        "将功能需求映射到微服务与技术组件，并补充非功能需求的架构级实现策略。"
    )
    page_break(doc)

    doc.add_heading("2 总体架构设计", level=1)
    doc.add_heading("2.1 架构风格", level=2)
    add_body_text(
        doc,
        "系统采用「前后端分离 + 微服务 + 云原生」架构。"
        "客户端（Flutter / Vue3）通过 HTTPS 访问 Spring Cloud Gateway，"
        "网关基于 Path 路由至 Nacos 注册的业务微服务。"
        "服务间同步调用使用 OpenFeign，异步解耦使用 RabbitMQ。"
        "读多写少场景（搜索）使用 Elasticsearch；会话与 Token 使用 Redis。"
    )
    doc.add_heading("2.2 逻辑分层", level=2)
    add_table(
        doc,
        ["层次", "职责", "技术"],
        [
            ["展现层", "App / 管理后台 UI", "Flutter, Vue3, Element Plus"],
            ["网关层", "路由、鉴权、限流", "Spring Cloud Gateway"],
            ["业务层", "领域微服务", "Spring Boot 3, MyBatis-Plus"],
            ["AI 层", "大模型应用与 Tool", "Jeecg Airag, SSE"],
            ["数据层", "持久化与检索", "MySQL, Redis, ES"],
            ["集成层", "第三方", "华为 OAuth, 支付宝 SDK"],
        ],
    )
    doc.add_heading("2.3 微服务清单", level=2)
    rows = [[m["service"], m["name"], m["desc"][:40] + "…"] for m in MODULES if m["id"] != "app"]
    rows.insert(0, ["jeecg-system (7001)", "系统与 Airag", "基础系统、AI 大模型"])
    rows.insert(0, ["jeecg-gateway (9999)", "API 网关", "统一入口"])
    add_table(doc, ["服务", "名称", "说明"], rows)
    page_break(doc)

    doc.add_heading("2.4 部署架构", level=2)
    add_body_text(
        doc,
        "开发环境各微服务本地多端口启动；生产环境推荐 Docker Compose 或 K8s 部署，"
        "MySQL/Redis/ES/RabbitMQ/Nacos 独立集群。"
        "Gateway 对外暴露 443，内网服务不直接暴露公网。"
    )
    page_break(doc)

    section_count = 4
    doc.add_heading("3 模块概要设计", level=1)
    for i, mod in enumerate(MODULES, 1):
        doc.add_heading(f"3.{i} {mod['name']}", level=2)
        add_body_text(doc, mod["desc"])
        doc.add_heading("3.{}.1 职责边界".format(i), level=3)
        add_bullet_list(doc, mod["features"])
        doc.add_heading("3.{}.2 对外接口".format(i), level=3)
        add_body_text(
            doc,
            f"REST API 前缀见 gateway_fondia_routes.json。"
            f"服务注册名：{mod['service']}。"
        )
        doc.add_heading("3.{}.3 依赖关系".format(i), level=3)
        add_body_text(
            doc,
            "依赖 Nacos 服务发现、MySQL 主库、Redis 缓存。"
            "若模块需搜索则 Feign 调用 jeecg-search；"
            "若模块需 AI 则经 support 或直接 Feign jeecg-system。"
        )
        page_break(doc)
        section_count += 1

    doc.add_heading("4 数据库概要设计", level=1)
    add_body_text(
        doc,
        "采用 MySQL 8.0，库表按微服务逻辑分库（可物理同库不同前缀）。"
        "主键统一 bigint 雪花 ID；通用字段 create_by/create_time/update_by/update_time。"
    )
    doc.add_heading("5 安全设计", level=1)
    add_bullet_list(
        doc,
        [
            "JWT Bearer Token 鉴权，网关与 Shiro 协同",
            "管理端 RBAC 菜单按钮级权限",
            "密码 BCrypt 加密",
            "支付回调 RSA 验签",
            "AI 接口异步线程池隔离 Shiro 上下文",
        ],
    )
    doc.add_heading("6 AI 子系统概要设计", level=1)
    add_body_text(
        doc,
        "AI 对话链路：App → support/ai/chat (SSE) → CsAiServiceImpl → "
        "jeecg-system /airag/chat/send → 大模型 + Tool(search_fondia_recipe)。"
        "流式事件协议：MESSAGE/THINKING/TOOL_EXEC_*/APP_META。"
        "客户端 AiStreamTextAccumulator 只追加 MESSAGE 文本，不过滤工具标记。"
    )

    fill_to_pages(doc, section_count + 3, "hld")
    return doc


def build_detail_doc() -> Document:
    doc = Document()
    setup_styles(doc)
    add_cover(doc, "Fondia 食养平台", "软件详细设计说明书")
    add_toc_placeholder(doc)

    doc.add_heading("1 引言", level=1)
    add_body_text(
        doc,
        "本文档在概要设计基础上，给出类设计、接口规格、数据库表结构、"
        "关键流程时序及异常处理等实现级细节，供开发人员直接编码参考。"
    )
    page_break(doc)

    doc.add_heading("2 技术规范", level=1)
    add_table(
        doc,
        ["规范项", "约定"],
        [
            ["Java 包名", "org.jeecg.modules.{module}"],
            ["API 响应", "Result<T>，code=200 成功"],
            ["分页", "pageNo, pageSize, IPage"],
            ["日期格式", "yyyy-MM-dd HH:mm:ss"],
            ["ID 策略", "ASSIGN_ID 雪花"],
            ["日志", "Slf4j + Logback，关键业务 INFO"],
        ],
    )
    page_break(doc)

    section_count = 2
    doc.add_heading("3 模块详细设计", level=1)

    key_designs = [
        ("user", "LoginAppController", "POST /user/login/password", "密码登录，返回 Token"),
        ("user", "HuaweiLoginHandler", "POST /user/login/huawei", "华为 ID Token 校验"),
        ("cuisine", "FdRecipeAdminController", "POST /sys/fd/recipe/rebuildSearchIndex", "全量 ES 重建"),
        ("search", "RecipeSearchController", "GET /search/recipe", "关键词搜索"),
        ("support", "SupportController", "POST /support/ai/chat", "AI SSE 流式"),
        ("support", "FondiaRecipeToolsProvider", "Tool", "AI 菜谱搜索 Tool"),
        ("member", "AiQuotaAppController", "POST /member/ai-quota/consume", "AI 扣次"),
        ("payment", "PaymentAppController", "POST /payment/order/create", "创建支付单"),
    ]

    for i, mod in enumerate(MODULES, 1):
        doc.add_heading(f"3.{i} {mod['name']}", level=2)
        for j, feat in enumerate(mod["features"], 1):
            doc.add_heading(f"3.{i}.{j} {feat}", level=3)
            add_body_text(
                doc,
                f"【功能】{feat}\n"
                f"【所属服务】{mod['service']}\n"
                f"【设计要点】参数校验 → 权限检查 → 业务逻辑 → 持久化 → 返回 Result"
            )
            if j <= 2 and any(k[0] == mod["id"] for k in key_designs):
                for kd in key_designs:
                    if kd[0] == mod["id"]:
                        add_table(
                            doc,
                            ["类", "接口", "说明"],
                            [[kd[1], kd[2], kd[3]]],
                        )
                        break
        page_break(doc)
        section_count += 1

    doc.add_heading("4 核心流程详细设计", level=1)
    doc.add_heading("4.1 AI 流式对话", level=2)
    add_numbered_list(
        doc,
        [
            "App postSseStream POST /support/ai/chat",
            "FdCsSessionServiceImpl.chatWithAiStream 创建 SseEmitter",
            "csAiTaskExecutor 异步 runAiStreamTask",
            "CsAiServiceImpl.streamChat 转发 Airag SSE",
            "每条 payload 原样 emitter.send 给客户端",
            "流结束 matchForChat 解析菜谱，发送 APP_META",
            "App AiStreamTextAccumulator 累积 MESSAGE 文本",
        ],
    )
    doc.add_heading("4.2 AI 配额扣减", level=2)
    add_numbered_list(
        doc,
        [
            "App 发送前调用 /member/ai-quota/consume",
            "AiQuotaAppServiceImpl 查 fd_ai_quota_config",
            "非会员默认 2 次/天，会员按套餐配置",
            "超限抛 JeecgBootException，App 弹开通会员",
        ],
    )
    doc.add_heading("4.3 菜谱搜索索引同步", level=2)
    add_numbered_list(
        doc,
        [
            "管理端保存菜谱 → cuisine 服务发 RabbitMQ",
            "search 服务消费消息写 ES fd_recipe 索引",
            "全量重建：POST rebuildSearchIndex",
        ],
    )
    page_break(doc)

    doc.add_heading("5 数据库详细设计（核心表）", level=1)
    tables = [
        ("fd_user", "App 用户", ["id", "phone", "email", "nickname", "member_status", "member_expire_time"]),
        ("fd_recipe", "菜谱", ["id", "title", "cover_image", "category_id", "status", "calories"]),
        ("fd_cs_session", "客服/AI 会话", ["id", "user_id", "source", "ai_conversation_id", "last_message"]),
        ("fd_ai_quota_config", "AI 配额配置", ["id", "plan_code", "daily_limit", "feature_code"]),
        ("fd_member_order", "会员订单", ["id", "user_id", "plan_code", "amount", "pay_status"]),
    ]
    for tbl, cn, cols in tables:
        doc.add_heading(f"5.x 表 {tbl}（{cn}）", level=2)
        add_table(doc, ["字段", "说明"], [[c, "见 SQL 脚本"] for c in cols])

    fill_to_pages(doc, section_count + 6, "detail")
    return doc


def save_doc(doc: Document, filename: str) -> str:
    path = os.path.join(OUTPUT_DIR, filename)
    doc.save(path)
    return path


def main() -> None:
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print("正在生成需求分析文档…")
    p1 = save_doc(build_requirements_doc(), "Fondia_软件需求分析说明书.docx")
    print(f"  → {p1}")

    print("正在生成概要设计文档…")
    p2 = save_doc(build_hld_doc(), "Fondia_软件概要设计说明书.docx")
    print(f"  → {p2}")

    print("正在生成详细设计文档…")
    p3 = save_doc(build_detail_doc(), "Fondia_软件详细设计说明书.docx")
    print(f"  → {p3}")

    print("\n完成！请在 Word 中打开文档：")
    print("  1. 引用 → 目录 → 自动目录")
    print("  2. 查看 → 页数，确认约 100 页/份")
    print("  3. 可按需调整页眉页脚与公司 Logo")


if __name__ == "__main__":
    main()
