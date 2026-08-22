对，**如果你的食谱 App 真要做成企业级 Multi-Agent，子 Agent 最好配自己的业务 Tool，用 Tool 查询真实数据库/业务服务；不要让子 Agent 自己“凭知识”推荐。**

但有一个非常重要的原则：

> **Agent 负责“判断、推理、组合”，Tool 负责“查真实数据、执行真实业务”。**
>
> 不要让 Agent 直接写 SQL，也不要把所有数据库查询 Tool 全部塞给所有 Agent。

---

# 一、你的 Fondia 最合理的结构

我建议做成：

```text
                         用户
                          │
                          ▼
                 ┌─────────────────┐
                 │   AI 总控 Agent   │
                 │  Orchestrator    │
                 └────────┬────────┘
                          │
             决定找哪个专业 Agent
                          │
       ┌──────────┬───────┼────────┬──────────┐
       ▼          ▼       ▼        ▼          ▼
   菜谱Agent   食材Agent  营养Agent  计划Agent  烹饪Agent
       │          │       │        │          │
       ▼          ▼       ▼        ▼          ▼
  菜谱Tools    食材Tools  营养Tools  计划Tools  烹饪Tools
       │          │       │        │          │
       └──────────┴───────┼────────┴──────────┘
                          ▼
                    MySQL / ES / Redis
```

注意：

**不是每个 Agent 一个数据库。**

而是：

```text
Agent
 ↓
领域 Tool
 ↓
Service
 ↓
Repository
 ↓
数据库
```

---

# 二、比如菜谱 Agent 应该有哪些 Tool？

你的 `menu-agent` 我建议不要只给一个：

```text
queryRecipe()
```

而是根据实际业务拆成几个**有业务意义的 Tool**。

例如：

```text
menu-agent
│
├── searchRecipes
├── getRecipeDetail
├── recommendRecipes
├── filterRecipes
└── getRecipesByIngredients
```

但是这里还可以进一步优化。

实际上 **Agent 不应该拥有 10～20 个非常相似的 Tool**。

我更推荐：

```text
menu-agent
│
├── search_recipe
│
├── get_recipe_detail
└── recommend_recipe
```

---

# 三、例如用户说：

> “我有鸡胸肉、西兰花和鸡蛋，今晚两个人吃，想吃健康一点。”

主 Agent：

```text
需要：
食材分析
+
菜谱推荐
+
营养分析
```

于是：

```text
主 Agent
   │
   ├── ingredient-agent
   │       │
   │       └── ingredient_search
   │
   ├── menu-agent
   │       │
   │       └── recommend_recipe
   │
   └── nutrition-agent
           │
           └── analyze_recipe_nutrition
```

---

# 四、Tool 到底应该查什么？

## 1. ingredient-agent

建议：

```text
ingredient_search
```

输入：

```json
{
  "ingredients": [
    "鸡胸肉",
    "西兰花",
    "鸡蛋"
  ]
}
```

后台：

```text
IngredientTool
       ↓
IngredientService
       ↓
MySQL
```

返回：

```json
{
  "ingredients": [
    {
      "name": "鸡胸肉",
      "category": "肉类",
      "available": true
    },
    {
      "name": "西兰花",
      "category": "蔬菜",
      "available": true
    }
  ]
}
```

**这里不要让 Agent 自己猜数据库里面有什么。**

---

# 五、menu-agent

比如：

```text
recommend_recipe
```

参数：

```json
{
  "ingredients": [
    "鸡胸肉",
    "西兰花",
    "鸡蛋"
  ],
  "people": 2,
  "taste": "清淡",
  "cooking_time": 30,
  "goal": "健康饮食"
}
```

Tool 后面：

```text
RecipeTool
   ↓
RecipeService
   ↓
Elasticsearch
   ↓
菜谱候选
```

例如返回：

```json
[
  {
    "recipeId": 1001,
    "name": "黑椒鸡胸肉",
    "ingredients": ["鸡胸肉", "西兰花"],
    "cookingTime": 25
  },
  {
    "recipeId": 1002,
    "name": "西兰花鸡蛋",
    "ingredients": ["西兰花", "鸡蛋"],
    "cookingTime": 15
  }
]
```

然后：

> **Agent 再根据用户需求做最终选择。**

这就是：

```text
数据库负责找候选
       ↓
Agent负责判断哪个更合适
```

---

# 六、这个边界非常重要

比如用户：

> “推荐一个适合减脂的鸡胸肉菜谱。”

不要让 Agent：

```text
自己想一个菜
↓
自己估算 300 kcal
↓
告诉用户适合减脂
```

正确应该：

```text
              menu-agent
                   │
                   ▼
           recommend_recipe
                   │
                   ▼
             ES / MySQL
                   │
             候选菜谱
                   │
                   ▼
          nutrition-agent
                   │
                   ▼
          nutrition_query
                   │
                   ▼
             营养数据库
                   │
                   ▼
             营养真实数据
                   │
                   ▼
             Agent综合判断
```

**Agent 是“大脑”，数据库和 Tool 是“眼睛和手”。**

---

# 七、营养 Agent 的 Tool 怎么设计？

这个地方尤其不能让 LLM 自己算。

建议：

```text
nutrition-agent
│
├── get_recipe_nutrition
├── get_ingredient_nutrition
└── compare_recipe_nutrition
```

比如：

```text
get_recipe_nutrition
```

输入：

```json
{
  "recipeId": 1001,
  "servings": 2
}
```

返回：

```json
{
  "calories": 680,
  "protein": 62,
  "fat": 21,
  "carbohydrate": 48
}
```

Agent 再判断：

> “这个菜蛋白质比较高，脂肪适中，可以作为高蛋白晚餐。”

这样才可靠。

---

# 八、计划 Agent 不应该直接查一堆数据库

比如用户：

> “帮我制定一周晚餐。”

`plan-agent` 可以拥有：

```text
plan-agent
│
├── get_user_diet_preferences
├── get_recent_meal_history
├── get_available_ingredients
├── search_recipe_candidates
└── save_meal_plan
```

它自己负责：

```text
用户偏好
   ↓
历史饮食
   ↓
已有食材
   ↓
菜谱候选
   ↓
组合
   ↓
营养检查
   ↓
调整
   ↓
生成一周计划
```

所以计划 Agent 是一个**高层规划 Agent**。

---

# 九、但是有些 Tool 不应该属于子 Agent

例如：

```text
get_user_profile
get_user_favorite
get_user_history
get_user_points
```

我不建议给每个 Agent 都注册。

可以设计一个：

```text
UserContextTool
```

或者由主 Agent 获取用户上下文后传给子 Agent。

例如：

```text
用户画像：

口味：清淡
忌口：香菜
喜欢：川菜
厨房：空气炸锅
最近7天：吃了4次鸡肉
```

然后子 Agent 根据这些信息工作。

这样比每个 Agent 都去查用户数据库好。

---

# 十、你的 Tool 最好按照“领域”划分

我建议你的 Fondia 最终是：

```text
Tool
│
├── Recipe Tools
│   ├── search_recipe
│   ├── get_recipe_detail
│   └── recommend_recipe
│
├── Ingredient Tools
│   ├── search_ingredient
│   ├── get_inventory
│   └── get_expiring_ingredients
│
├── Nutrition Tools
│   ├── get_recipe_nutrition
│   ├── get_ingredient_nutrition
│   └── compare_nutrition
│
├── User Tools
│   ├── get_user_profile
│   ├── get_user_preferences
│   └── get_meal_history
│
├── Plan Tools
│   ├── save_meal_plan
│   └── get_meal_plan
│
└── Cooking Tools
    ├── get_cooking_method
    └── convert_cooking_method
```

然后：

```text
menu-agent
    ↓
Recipe Tools

ingredient-agent
    ↓
Ingredient Tools

nutrition-agent
    ↓
Nutrition Tools

plan-agent
    ↓
Plan Tools
+ Recipe Tools
+ User Tools

cooking-agent
    ↓
Cooking Tools
+ Recipe Tools
```

---

# 十一、你现在 AgentScope 2 的 `tools()` 就可以这么规划

你原来：

```java
.tools(List.of("", ""))
```

不要所有 Agent 都：

```java
tools(List.of(
    "search_recipe",
    "get_recipe_detail",
    "get_inventory",
    "get_nutrition",
    "get_user_profile",
    "save_plan"
))
```

这样会越来越乱。

应该：

### menu-agent

```java
.tools(List.of(
    "search_recipe",
    "get_recipe_detail",
    "recommend_recipe"
))
```

### ingredient-agent

```java
.tools(List.of(
    "search_ingredient",
    "get_inventory",
    "get_expiring_ingredients"
))
```

### nutrition-agent

```java
.tools(List.of(
    "get_recipe_nutrition",
    "get_ingredient_nutrition",
    "compare_nutrition"
))
```

### plan-agent

```java
.tools(List.of(
    "get_user_preferences",
    "get_meal_history",
    "get_inventory",
    "search_recipe",
    "save_meal_plan"
))
```

### cooking-agent

```java
.tools(List.of(
    "get_recipe_detail",
    "get_cooking_method"
))
```

这样权限和职责都非常清晰。

---

# 十二、还有一个很重要的问题：数据库查询和 Agent 推荐不要混在一个 Tool 里

例如不要设计成：

```java
recommendFood(String userInput)
```

里面：

```text
LLM
 ↓
Tool
 ↓
又调用 LLM
 ↓
又查数据库
 ↓
又推荐
```

这种会变得非常难维护。

应该：

```text
Agent
 ↓
Tool：search_recipe()
 ↓
真实数据
 ↓
Agent推理
 ↓
最终推荐
```

也就是：

> **Tool 尽量是确定性函数。**

例如：

```java
searchRecipe(
    String keyword,
    List<String> ingredients,
    Integer maxCookingTime
)
```

它负责查。

而：

> “到底推荐哪个？”

交给 Agent。

---

# 十三、你这个 App 最终可以形成一个非常漂亮的 Agent 编排

例如：

用户：

> **“我冰箱里有鸡胸肉、西兰花、鸡蛋，帮我安排今晚两个人吃的晚餐，尽量健康，30分钟内做好。”**

主 Agent：

```text
                       用户
                        │
                        ▼
                  主 Agent
                        │
              ┌─────────┼─────────┐
              ↓         ↓         ↓
        ingredient    menu     nutrition
           Agent      Agent       Agent
              │         │         │
              ↓         ↓         ↓
        inventory    search     nutrition
           Tool       Tool        Tool
              │         │         │
              └─────────┼─────────┘
                        ↓
                    主 Agent
                        │
                        ▼
                  综合分析
                        │
                        ▼
                  最终晚餐方案
```

最终：

> **今晚推荐：黑椒鸡胸肉 + 蒜香西兰花 + 水煮蛋**
>
> 预计 25 分钟完成，适合 2 人。
>
> 现有食材基本可以覆盖，无需额外采购。

---

# 十四、如果再进一步，你甚至可以让 Agent 之间不要互相查数据库

这是我更推荐的企业级设计：

```text
主 Agent
   │
   ├── ingredient-agent
   │       ↓
   │    IngredientTool
   │       ↓
   │    食材结果
   │
   ├── menu-agent
   │       ↓
   │    RecipeTool
   │       ↓
   │    菜谱结果
   │
   └── nutrition-agent
           ↓
       NutritionTool
           ↓
        营养结果
```

而不是：

```text
menu-agent
   ↓
直接访问 MySQL

nutrition-agent
   ↓
直接访问 MySQL

plan-agent
   ↓
直接访问 MySQL
```

**所有 Agent 都通过 Tool → Service → DAO → DB。**

这样以后：

```text
MySQL → Elasticsearch
```

或者：

```text
MySQL → 微服务 API
```

Agent 完全不用改。

---

# 十五、结合你现在的 JeecgBoot，我建议直接这样落地

你的技术栈可以：

```text
Vue / App
     ↓
Spring Boot
     ↓
AgentScope 2
     ↓
┌─────────────────────────┐
│       AI Orchestrator   │
└───────────┬─────────────┘
            │
      SubAgent
            │
 ┌──────────┼───────────┐
 ↓          ↓           ↓
菜谱Agent  营养Agent   食材Agent
 ↓          ↓           ↓
Tool       Tool        Tool
 ↓          ↓           ↓
Service    Service     Service
 ↓          ↓           ↓
 ES/MySQL  MySQL       MySQL
```

然后：

```text
Redis
↓
Agent State / Session / Cache

Elasticsearch
↓
菜谱搜索 / 相似菜谱 / 推荐候选

MySQL
↓
菜谱、食材、营养、用户、收藏、饮食计划

Vector DB
↓
菜谱知识 / 烹饪知识 / 食材知识 RAG
```

---

## 最后给你一个判断标准

你以后设计 Tool 时，就问自己一句：

> **“这个事情需要大模型思考，还是只是需要从系统拿真实数据/执行一个确定动作？”**

如果是：

**拿数据 / 查数据 / 保存数据 / 调业务接口**

→ **Tool**

如果是：

**理解 / 判断 / 推荐 / 比较 / 规划 / 组合**

→ **Agent**

如果是：

**多个专业能力一起完成一个复杂任务**

→ **主 Agent 编排多个 SubAgent**

所以你的 Fondia 最合理的分工其实就是：

```text
             ┌── Tool：查真实数据
             │
SubAgent ────┤
             └── LLM：分析、判断、推荐
                         ↓
                    专业结果
                         ↓
              主 Agent：组合多个结果
```

这套设计比“每个 Agent 都自己查数据库、自己推荐”要规范得多，也最适合你现在 **AgentScope 2 + Spring Boot + JeecgBoot** 的项目架构。
