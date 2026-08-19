你这段代码**已经走到 AgentScope 2 Harness Subagent 的正确方向了**，但现在有一个非常关键的问题：

> **你最后调用了 `.disableSubagents()`，这会直接把多 Agent 能力关闭。**

所以你现在虽然写了：

```java
builder.subagents(subagentDeclarationList);
```

但前面已经：

```java
.disableSubagents();
```

这两个是冲突的。官方文档明确说，只有当前 Agent **没有调用 `disableSubagents()`**，并且配置了 model 等条件时，Harness 才会装载 `SubagentsHook`，从而向主 Agent 暴露 `agent_spawn`、`agent_send`、`agent_list` 等工具。([AgentScope][1])

---

# 一、你这个项目正确的工作方式

你的食谱 App 可以做成：

```text
                         用户
                          │
                          ▼
                 ┌────────────────┐
                 │   主 Agent      │
                 │  食谱AI总管      │
                 └───────┬────────┘
                         │
                  LLM 自己判断
                         │
       ┌─────────┬───────┼────────┬─────────┐
       ▼         ▼       ▼        ▼         ▼
    菜谱Agent  营养Agent 食材Agent 计划Agent 烹饪Agent
       │         │       │        │         │
       └─────────┴───────┼────────┴─────────┘
                         ▼
                    主 Agent 汇总
                         │
                         ▼
                       用户
```

这里最重要的是：

**你不需要在 Java 里面写：**

```java
if ("菜谱".equals(...)) {
    menuAgent.xxx();
}
```

而是：

```text
主 Agent
   ↓
LLM 判断
   ↓
发现：
menu-agent = 菜谱查询
pabulum-agent = 营养分析
ingredient-agent = 食材分析
plan-agent = 饮食计划
   ↓
调用 agent_spawn
```

AgentScope 2 的 Subagent 机制就是这么设计的：`SubagentDeclaration` 声明子 Agent 后，框架会把可用子 Agent 的 `agent_id` 和描述注入主 Agent；主 Agent 在推理过程中可以使用 `agent_spawn`、`agent_send` 等工具进行委派。([AgentScope][1])

---

# 二、你代码首先改这里

你现在：

```java
builder
    .name(properties.getAgentName())
    .sysPrompt(systemText)
    .model(chatModel)
    ...
    .disableSubagents();
```

**直接删掉：**

```java
.disableSubagents();
```

也就是：

```java
HarnessAgent.Builder builder = HarnessAgent.builder()
        .name(properties.getAgentName())
        .sysPrompt(systemText)
        .model(chatModel)
        .toolkit(toolkit)
        .workspace(Path.of(properties.getWorkspace()))
        .stateStore(agentStateStore)
        .middlewares(List.of(
                new CompactionHintMiddleware(
                        properties.getMemory().getCompactionTriggerMessages(),
                        properties.getMemory().isCompactionEnabled()),
                new FullObservabilityMiddleware()
        ))
        .memory(memoryConfig)
        .permissionContext(permissionContextState)
        .toolResultEviction(ToolResultEvictionConfig.defaults());
```

然后后面：

```java
if (memory.isSubagentEable()) {
    builder.subagent(...);
}
```

或者继续用你现在的：

```java
builder.subagents(subagentDeclarationList);
```

---

# 三、但是你现在还有一个问题：SubAgent 没有真正的 Prompt

你现在写：

```java
SubagentDeclaration menuAgent = SubagentDeclaration.builder()
        .name("menu-agent")
        .description("菜谱Agent")
        .workspace(Path.of(".rydAgent/menu"))
        .workspaceMode(WorkspaceMode.ISOLATED)
        .model("qwen3-max")
        .steps(8)
        .tools(List.of("", ""))
        .build();
```

这里：

```java
.description("菜谱Agent")
```

**太简单了。**

因为 description 是主 Agent 判断“什么时候应该调用这个 Agent”的重要依据。

官方文档甚至明确把 `description` 标成“agent 选择是否委派的关键依据”。([AgentScope][2])

应该改成：

```java
.description("""
    菜谱推荐专家。
    用于根据用户的口味、已有食材、饮食目标、菜系、烹饪时间等条件，
    从食谱数据库中查询和推荐合适的菜谱。
    负责菜谱搜索、菜谱筛选、菜谱推荐。
    不负责营养计算、用户画像和一周饮食计划。
    """)
```

这样 LLM 才知道：

> **什么时候应该找 menu-agent。**

---

# 四、你的 5 个 Agent 我建议这样设计

你现在：

```text
menu-agent
pabulum-agent
ingredient-agent
plan-agent
user-agent
```

我建议调整成：

```text
menu-agent
nutrition-agent
ingredient-agent
plan-agent
cooking-agent
```

**user-agent 我反而不建议做 Agent。**

用户画像：

```text
用户喜欢什么
用户不喜欢什么
收藏记录
点赞记录
历史菜谱
饮食偏好
```

这些更适合做成：

```text
UserProfileTool
```

或者：

```text
UserPreferenceService
```

因为：

> 查询用户画像是确定性业务，不需要 LLM 再推理一次。

---

# 五、例如这样定义 menu-agent

你可以直接这样：

```java
SubagentDeclaration menuAgent =
        SubagentDeclaration.builder()
                .name("menu-agent")
                .description("""
                        菜谱推荐专家。

                        负责：
                        1. 根据用户需求搜索菜谱
                        2. 根据已有食材推荐菜谱
                        3. 根据菜系、口味、烹饪时间筛选菜谱
                        4. 根据饮食目标推荐菜谱
                        5. 查询菜谱详细制作信息

                        适用场景：
                        - 今天吃什么
                        - 推荐几个鸡胸肉做法
                        - 我有番茄、鸡蛋，能做什么
                        - 推荐一道川菜
                        - 30分钟内能做什么

                        不负责：
                        - 营养数据计算
                        - 用户画像分析
                        - 一周饮食计划
                        - 实际烹饪指导
                        """)
                .workspace(Path.of(".rydAgent/menu"))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .model("qwen3-max")
                .steps(8)
                .build();
```

---

# 六、营养 Agent

```java
SubagentDeclaration nutritionAgent =
        SubagentDeclaration.builder()
                .name("nutrition-agent")
                .description("""
                        营养分析专家。

                        负责：
                        1. 分析菜谱营养成分
                        2. 查询热量、蛋白质、脂肪、碳水等数据
                        3. 判断菜谱是否符合用户饮食目标
                        4. 对多个菜谱进行营养对比
                        5. 对一日三餐进行营养结构分析

                        适用场景：
                        - 这道菜热量高吗
                        - 这道菜适合减脂吗
                        - 哪个菜蛋白质更高
                        - 帮我分析今天的饮食

                        不负责：
                        - 菜谱搜索
                        - 用户画像
                        - 创建饮食计划
                        """)
                .workspace(Path.of(".rydAgent/nutrition"))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .model("qwen3-max")
                .steps(8)
                .build();
```

---

# 七、食材 Agent

```java
SubagentDeclaration ingredientAgent =
        SubagentDeclaration.builder()
                .name("ingredient-agent")
                .description("""
                        食材管理专家。

                        负责：
                        1. 识别用户提供的食材
                        2. 分析食材适合制作哪些菜
                        3. 分析食材之间的搭配
                        4. 根据库存和保质期推荐优先消耗的食材
                        5. 生成食材采购建议

                        适用场景：
                        - 我有鸡蛋、西红柿、牛肉，能做什么
                        - 冰箱里这些东西快过期了怎么办
                        - 帮我清理冰箱
                        - 还缺哪些食材

                        不负责：
                        - 营养计算
                        - 菜谱最终推荐
                        - 一周饮食计划
                        """)
                .workspace(Path.of(".rydAgent/ingredient"))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .model("qwen3-max")
                .steps(8)
                .build();
```

---

# 八、计划 Agent

```java
SubagentDeclaration planAgent =
        SubagentDeclaration.builder()
                .name("plan-agent")
                .description("""
                        饮食计划专家。

                        负责：
                        1. 制定一日三餐计划
                        2. 制定一周饮食计划
                        3. 根据用户饮食目标安排菜单
                        4. 控制菜谱重复率
                        5. 根据营养分析结果调整菜单
                        6. 生成一周采购清单

                        适用场景：
                        - 帮我安排明天三餐
                        - 帮我制定一周菜单
                        - 给我做一个家庭一周食谱
                        - 根据已有食材制定计划

                        不负责：
                        - 单独查询菜谱详情
                        - 用户画像查询
                        - 底层营养数据计算
                        """)
                .workspace(Path.of(".rydAgent/plan"))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .model("qwen3-max")
                .steps(10)
                .build();
```

---

# 九、然后最关键：每个 Agent 的 workspace 里面放什么？

你现在：

```java
.workspace(Path.of(".rydAgent/menu"))
```

这个思路是对的。

但要注意，官方的 `workspace` 声明模式是把它作为 **Agent 定义目录**，通常放 `AGENTS.md`；也可以用 Markdown spec 文件方式定义子 Agent。([AgentScope][2])

比如：

```text
.rydAgent/
│
├── menu/
│   └── AGENTS.md
│
├── nutrition/
│   └── AGENTS.md
│
├── ingredient/
│   └── AGENTS.md
│
├── plan/
│   └── AGENTS.md
│
└── cooking/
    └── AGENTS.md
```

例如：

```text
.rydAgent/menu/AGENTS.md
```

里面写：

```markdown
# 菜谱专家

你是一名专业的菜谱推荐专家。

## 职责

- 查询菜谱
- 推荐菜谱
- 根据食材匹配菜谱
- 根据口味筛选菜谱
- 根据烹饪时间筛选菜谱

## 工作原则

不要自己编造数据库中不存在的菜谱。

需要查询数据时使用菜谱查询工具。

最终输出：
- 菜谱名称
- 推荐理由
- 主要食材
- 烹饪时间
```

这样：

```text
description
```

解决的是：

> **主 Agent 为什么要调用我？**

而：

```text
AGENTS.md
```

解决的是：

> **调用我以后，我具体怎么干？**

这两个概念一定要分开。

---

# 十、你现在的 `.tools(List.of("", ""))` 也有问题

你写：

```java
.tools(List.of("", ""))
```

这个不能这么写。

应该是实际注册到父 Agent Toolkit 里的工具名称。

例如你注册：

```java
toolkit.registerTool(new RecipeSearchTool());
toolkit.registerTool(new NutritionTool());
toolkit.registerTool(new IngredientTool());
```

那么子 Agent：

```java
.tools(List.of(
    "recipe_search"
))
```

具体名称以你 `@Tool` / `Toolkit` 注册后的工具名为准。

官方文档说明，Subagent 可以配置 `tools` 白名单；如果不指定，则按继承规则处理。([AgentScope][2])

---

# 十一、然后最关键的问题：主 Agent 到底怎么“编排”？

假设用户说：

> **“我家里有鸡胸肉、西兰花、鸡蛋，今晚两个人吃，想吃得健康一点，帮我安排一下。”**

主 Agent 收到以后。

它看到：

```text
menu-agent
菜谱推荐

nutrition-agent
营养分析

ingredient-agent
食材分析

plan-agent
饮食计划
```

LLM 会自己形成类似这样的决策：

```text
用户需求：
安排今晚晚餐

需要：
1. 根据现有食材推荐菜谱
2. 分析营养
3. 最终形成晚餐方案
```

于是可能：

```text
agent_spawn
    ↓
ingredient-agent
```

得到：

```text
鸡胸肉
西兰花
鸡蛋

可以组合：
鸡胸肉西兰花
鸡蛋作为配菜
```

然后：

```text
agent_spawn
    ↓
menu-agent
```

得到：

```text
黑椒鸡胸肉
西兰花炒鸡胸
鸡胸肉沙拉
```

然后：

```text
agent_spawn
    ↓
nutrition-agent
```

得到：

```text
黑椒鸡胸肉
热量 XXX
蛋白质 XXX
...
```

最后主 Agent：

```text
        ingredient-agent
               ↓
          食材分析
               ↓
         menu-agent
               ↓
          菜谱候选
               ↓
       nutrition-agent
               ↓
          营养分析
               ↓
            主 Agent
               ↓
           最终推荐
```

**这就是你要的多 Agent 编排。**

而且不是 Java 写死的流程，是 LLM 在 `agent_spawn` 工具可用后，根据各 Subagent 的 description 自主选择。框架会在每轮推理前通过 `SubagentsHook` 把子 Agent 能力注入系统上下文。([AgentScope][1])

---

# 十二、还有一个非常重要的地方：你的主 Prompt 要改

你现在：

```java
String systemText = systemPromptConfig.getAgentSystemMessage().get();
```

这个 Prompt 里面一定要明确告诉主 Agent：

```text
你是 Fondia AI 饮食总管。

你可以调用以下专业子 Agent：

menu-agent：
负责菜谱搜索和推荐。

nutrition-agent：
负责营养分析。

ingredient-agent：
负责食材分析和库存规划。

plan-agent：
负责一日三餐和一周饮食计划。

cooking-agent：
负责烹饪方法和做法调整。

工作原则：

1. 简单问题直接回答。
2. 涉及专业任务时优先委派给对应子 Agent。
3. 不要自己编造数据库信息。
4. 一个任务需要多个专业能力时，可以依次调用多个子 Agent。
5. 没有依赖关系的任务可以并行委派。
6. 子 Agent 返回结果后，再综合回答用户。
7. 不要向用户暴露内部 Agent 编排过程。
```

不过要注意：

**不需要把 `agent_spawn` 的具体调用格式硬编码到 Prompt。**

框架会给主 Agent 注入工具和规则。官方文档明确说明 `SubagentsHook` 会暴露 `agent_spawn` / `agent_send` / `agent_list` 等能力。([AgentScope][1])

---

# 十三、你现在的代码最终应该改成这种结构

你现有代码保留大部分不用动：

```java
HarnessAgent.Builder builder = HarnessAgent.builder()
        .name(properties.getAgentName())
        .sysPrompt(systemText)
        .model(chatModel)
        .toolkit(toolkit)
        .workspace(Path.of(properties.getWorkspace()))
        .stateStore(agentStateStore)
        .middlewares(List.of(
                new CompactionHintMiddleware(
                        properties.getMemory().getCompactionTriggerMessages(),
                        properties.getMemory().isCompactionEnabled()),
                new FullObservabilityMiddleware()
        ))
        .memory(memoryConfig)
        .permissionContext(permissionContextState)
        .toolResultEviction(ToolResultEvictionConfig.defaults());

if (memory.isSubagentEable()) {

    builder.subagents(List.of(

            SubagentDeclaration.builder()
                    .name("menu-agent")
                    .description("""
                            菜谱推荐专家。
                            负责菜谱搜索、菜谱筛选、菜谱推荐。
                            根据用户口味、食材、饮食目标和烹饪时间推荐菜谱。
                            """)
                    .workspace(Path.of(".rydAgent/menu"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .build(),

            SubagentDeclaration.builder()
                    .name("nutrition-agent")
                    .description("""
                            营养分析专家。
                            负责菜谱营养分析、热量分析、营养结构分析和饮食目标匹配。
                            """)
                    .workspace(Path.of(".rydAgent/nutrition"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .build(),

            SubagentDeclaration.builder()
                    .name("ingredient-agent")
                    .description("""
                            食材专家。
                            负责食材识别、食材搭配、库存分析、过期食材处理和采购建议。
                            """)
                    .workspace(Path.of(".rydAgent/ingredient"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .build(),

            SubagentDeclaration.builder()
                    .name("plan-agent")
                    .description("""
                            饮食规划专家。
                            负责一日三餐、一周菜单、饮食计划和采购清单生成。
                            """)
                    .workspace(Path.of(".rydAgent/plan"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(10)
                    .build()
    ));
}
```

**注意：不要再 `.disableSubagents()`。**

---

# 十四、你可以怎么验证到底有没有编排成功？

这个特别重要。

你现在已经有：

```java
new FullObservabilityMiddleware()
```

所以可以重点看日志。

然后测试：

### 测试 1

输入：

> “推荐一道川菜。”

理论上应该：

```text
主 Agent
 ↓
agent_spawn
 ↓
menu-agent
 ↓
结果
 ↓
主 Agent
```

### 测试 2

输入：

> “这道菜热量高不高？”

应该：

```text
主 Agent
 ↓
nutrition-agent
```

### 测试 3

输入：

> “我有鸡蛋、西红柿、牛肉，帮我安排今晚两个人的晚餐。”

这时候才是你真正想要的：

```text
主 Agent
 │
 ├── ingredient-agent
 │
 ├── menu-agent
 │
 └── nutrition-agent
 │
 ↓
综合
 ↓
最终晚餐方案
```

---

# 十五、还有一个你特别容易踩的坑：你以为“配置了 5 个 Agent”就是编排

不是。

```java
builder.subagents(...)
```

只是：

> **把 5 个子 Agent 的能力注册给主 Agent。**

真正的运行过程是：

```text
                 ┌──────────────────┐
                 │     主 Agent      │
                 └────────┬─────────┘
                          │
                       LLM 推理
                          │
             “我需要哪个专业能力？”
                          │
              ┌───────────┼───────────┐
              ↓           ↓           ↓
         agent_spawn  agent_spawn  agent_spawn
              ↓           ↓           ↓
          menu-agent  nutrition   ingredient
              │           │           │
              └───────────┼───────────┘
                          ↓
                      主 Agent
                          ↓
                       LLM再次推理
                          ↓
                       最终回答
```

所以：

> **SubagentDeclaration = Agent 能力注册**
>
> **agent_spawn = 真正发起子 Agent 任务**
>
> **description = 帮主 Agent 判断“该找谁”**
>
> **AGENTS.md / workspace = 告诉子 Agent“具体怎么干”**
>
> **主 Agent Prompt = 告诉总管整体业务规则**

这几个概念弄清楚，你这个食谱 App 的多 Agent 架构就基本通了。

---

## 最后，你这个项目我建议再做一个变化

你的“用户 Agent”先不要做。

做成：

```text
              Fondia AI 总管
                    │
       ┌────────────┼────────────┐
       ↓            ↓            ↓
    菜谱Agent     食材Agent    营养Agent
       │            │            │
       └────────────┼────────────┘
                    ↓
                 计划Agent
                    │
                    ↓
                 烹饪Agent
```

然后：

```text
UserProfileService
       ↑
       │
主 Agent / 各 SubAgent
       │
       ↓
UserProfileTool
```

这样架构会更合理。

**查询用户画像、收藏、历史记录、饮食偏好 → Tool / Service；**

**菜谱推荐、食材规划、营养推理、饮食计划、烹饪方案 → SubAgent。**

这会比“所有东西都做 Agent”专业很多。

另外，AgentScope 2 的文档还专门把 **Pipeline、Routing、Subagents、Supervisor、Handoffs** 分成不同的多 Agent 编排模式。你现在这个 Fondia 方案实际上最适合先用 **Subagent（主 Agent 动态委派）**，之后再把“一周饮食计划”做成 **Supervisor + Pipeline/并行**，那就是真正比较完整的 Agent 编排了。([AgentScope][3])

[1]: https://java.agentscope.io/zh/harness/subagent.html?utm_source=chatgpt.com "子 Agent（Subagent） - AgentScope Java"
[2]: https://java.agentscope.io/v2/zh/docs/harness/subagent.html?utm_source=chatgpt.com "子 Agent（Subagent） - AgentScope Java"
[3]: https://java.agentscope.io/v2/zh/docs/harness/subagent.html "子 Agent（Subagent） - AgentScope Java"
