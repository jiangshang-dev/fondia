# V25 AI 页：SSE、Markdown、卡片与配额

> **所属模块**：M5  ·  **建议时长**：60～80 分钟  ·  **形式**：实操重点课
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 5.5

## 一、学习目标

1. 接入 aiChatStream
2. 流式渲染 Markdown
3. 历史会话与配额弹窗

## 二、课前准备

- /support/ai/chat 可用
- 会员配额接口可用

## 三、口播 / 讲解提纲

### 3.1 流式 UI

- 气泡列表
- Markdown
- 滚动跟随

### 3.2 会话

- 新建/历史/活跃会话 API

### 3.3 配额

- status / consume
- 不足弹窗

## 四、屏幕演示步骤

1. AiExclusive.dart 关键路径
2. sse_client 累积文本
3. 点击菜谱卡片跳转
4. ai_quota_helper

## 五、课堂实验 / 作业

1. 真机完成：提问 → 流式回答 → 点卡片进详情
2. 无配额时出现引导

## 六、本节小结（片尾 1 分钟）

- SSE 是体验核心
- 配额先 consume 再发消息

**下节预告**：V26 我的与会员


## 七、关键文件 / 参考

- `fondia_app/lib/pages/Ai/AiExclusive.dart`
- `fondia_app/lib/api/support.dart`
- `fondia_app/lib/utils/ai_quota_helper.dart`
