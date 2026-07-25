# V18 SSE 流式协议与客户端防回退

> **所属模块**：M4  ·  **建议时长**：45～60 分钟  ·  **形式**：讲解 + 双端对照
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 4.2

## 一、学习目标

1. 掌握 MESSAGE/APP_* 事件语义
2. 读懂 Flutter sse_client 累积逻辑
3. 避免 THINKING/工具噪声污染正文

## 二、课前准备

- 打开 sse_client.dart、AiExclusive.dart

## 三、口播 / 讲解提纲

### 3.1 事件

- MESSAGE：增量文本
- APP_LINK / APP_META：卡片与收尾
- APP_ERROR：失败

### 3.2 客户端

- Accept: text/event-stream
- 按 data: 行解析 JSON
- peakText/防回退策略

## 四、屏幕演示步骤

1. 抓一条完整 SSE 日志
2. 逐步解释 AiStreamTextAccumulator

## 五、课堂实验 / 作业

1. 人为制造工具事件，确认 UI 不闪回清空

## 六、本节小结（片尾 1 分钟）

- MESSAGE 只追加不重置
- 过滤 ::jeecgToolExec:: 等内部片段

**下节预告**：V19 Tool Calling


## 七、关键文件 / 参考

- `fondia_app/lib/utils/sse_client.dart`
- `fondia_app/lib/pages/Ai/AiExclusive.dart`
