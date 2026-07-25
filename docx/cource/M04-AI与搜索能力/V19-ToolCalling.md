# V19 Tool Calling：搜索菜谱并推送卡片

> **所属模块**：M4  ·  **建议时长**：40～55 分钟  ·  **形式**：讲解 + 演示
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 4.3

## 一、学习目标

1. 理解 SearchTool 调用链
2. 流结束顺序：APP_LINK → APP_META
3. deep link fondia://recipe/{id}

## 二、课前准备

- search 服务可用

## 三、口播 / 讲解提纲

### 3.1 服务端

- 注册 Tool
- 调用 jeecg-search
- 组装 recipe 卡片 VO

### 3.2 推送时机

- 正文流式中/结束后
- 先 LINK 再 META（按现网约定）

## 四、屏幕演示步骤

1. 打断点看 Tool 入参出参
2. App 点击卡片跳转详情

## 五、课堂实验 / 作业

1. 让 AI 触发一次搜索工具并成功跳转详情页

## 六、本节小结（片尾 1 分钟）

- Tool 结果要可落到消息 extraJson
- 链接协议双端统一

**下节预告**：V20 向量检索扩展


## 七、关键文件 / 参考

- `SearchTool.java`
- `AiRecipeMatchServiceImpl.java`
