package org.jeecg.modules.ai.config;

import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
import org.jeecg.modules.ai.tools.FondiaToolNames;

import java.nio.file.Path;
import java.util.List;

/**
 * Fondia 子 Agent 声明。
 * <p>
 * description：给主 Agent 判断「何时委派」。<br>
 * workspace/AGENTS.md：给子 Agent 判断「被叫到以后怎么干」。<br>
 * 不指定 model，继承父 Agent 的 Model Bean。
 */
public final class FondiaSubagentDeclarations {

    private FondiaSubagentDeclarations() {
    }

    public static List<SubagentDeclaration> all(Path workspaceRoot) {
        return List.of(menu(workspaceRoot),
                nutrition(workspaceRoot),
                ingredient(workspaceRoot),
                plan(workspaceRoot),
                cooking(workspaceRoot)
        );
    }

    private static SubagentDeclaration menu(Path workspaceRoot) {
        return base(workspaceRoot, "menu-agent", 8, """
                菜谱推荐专家。用户问今天吃什么、按食材/菜系/口味/烹饪时间/饮食目标搜菜或推荐菜谱时使用。
                负责：菜谱搜索、筛选、推荐、查询制作信息。
                适用：今天吃什么、推荐几个鸡胸肉做法、我有番茄鸡蛋能做什么、推荐一道川菜、30分钟内能做什么。
                不负责：营养数据计算、用户画像分析、一周饮食计划、实际烹饪改法。
                """, FondiaToolNames.menuAgent());
    }

    private static SubagentDeclaration nutrition(Path workspaceRoot) {
        return base(workspaceRoot, "nutrition-agent", 8, """
                营养分析专家。用户问热量、蛋白质、适不适合减脂、菜谱营养对比或一日三餐营养结构时使用。
                负责：营养成分分析、饮食目标匹配、多菜对比。
                适用：这道菜热量高吗、适不适合减脂、哪个菜蛋白质更高、帮我分析今天的饮食。
                不负责：菜谱搜索、用户画像、创建饮食计划。
                """, FondiaToolNames.nutritionAgent());
    }

    private static SubagentDeclaration ingredient(Path workspaceRoot) {
        return base(workspaceRoot, "ingredient-agent", 8, """
                食材管理专家。用户报冰箱/手头食材、问能搭配什么、快过期先吃什么、还缺什么时使用。
                负责：食材识别、搭配分析、优先消耗、采购缺口。
                适用：我有鸡蛋西红柿牛肉能做什么、冰箱东西快过期了怎么办、帮我清理冰箱、还缺哪些食材。
                不负责：营养计算、最终点哪几道菜、一周饮食计划。
                """, FondiaToolNames.ingredientAgent());
    }

    /**
     * 饮食计划专家 agent
     */
    private static SubagentDeclaration plan(Path workspaceRoot) {
        return SubagentDeclaration.builder()
                .name("plan-agent")
                .description("""
                        饮食计划专家。用户要安排三餐、一周菜单、家庭食谱或根据已有食材做计划时使用。
                        负责：一日三餐、一周菜单、控制重复率、结合营养结果调整、生成采购清单。
                        适用：帮我安排明天三餐、制定一周菜单、家庭一周食谱、根据已有食材制定计划。
                        不负责：单独查询菜谱详情、用户画像查询、底层营养数据计算。
                        """)
                .workspace(workspaceRoot.resolve("plan"))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .steps(12)
                .tools(FondiaToolNames.planAgent())
                .build();
    }

    private static SubagentDeclaration cooking(Path workspaceRoot) {
        return SubagentDeclaration.builder()
                .name("cooking-agent")
                .description("""
                        烹饪指导专家。用户已有具体菜谱，需要改做法、替食材、换厨具、调整口感或忌口时使用。
                        负责：步骤讲解、食材替代、空气炸锅/平底锅等设备改法、少辣/软烂/少油调整。
                        适用：没有黑胡椒怎么办、只有空气炸锅怎么做、鸡胸肉怎么做不柴、家里有老人能不能做得软一点。
                        不负责：从零推荐菜单、营养精确计算、制定一周计划。
                        """)
                .workspace(workspaceRoot.resolve("cooking"))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .steps(8)
                .persistSession(true)
                .tools(FondiaToolNames.cookingAgent())
                .build();
    }

    private static SubagentDeclaration base(Path workspaceRoot, String name, int steps, String description, List<String> tools) {
        String dir = name.endsWith("-agent") ? name.substring(0, name.length() - "-agent".length()) : name;
        return SubagentDeclaration.builder()
                .name(name)
                .description(description)
                .workspace(workspaceRoot.resolve(dir))
                .workspaceMode(WorkspaceMode.ISOLATED)
                .steps(steps)
                .tools(tools)
                .build();
    }
}
