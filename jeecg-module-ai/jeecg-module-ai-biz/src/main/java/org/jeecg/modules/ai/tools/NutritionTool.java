package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.NutritionFacts;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jeecg.modules.ai.tools.FondiaToolNames.COMPARE_NUTRITION;
import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_INGREDIENT_NUTRITION;
import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_RECIPE_NUTRITION;

/**
 * 营养领域 Tool：只返回库里的数字，禁止在 Tool 内口算或写「能不能吃」的结论。
 */
@Component
public class NutritionTool {

    @Tool(name = GET_RECIPE_NUTRITION, description = "按菜谱ID查询热量、蛋白质、脂肪、碳水等营养数据，数字来自营养库")
    public ToolResult<NutritionFacts> getRecipeNutrition(
            @ToolParam(name = "recipe_id", description = "菜谱ID") Long recipeId,
            @ToolParam(name = "servings", description = "份数，默认 1") Integer servings) {
        if (recipeId == null) {
            return ToolResult.fail("INVALID_PARAM", "recipe_id 不能为空");
        }
        // TODO 接入菜谱营养表，不要让 LLM 估算
        return ToolResult.notImplemented(GET_RECIPE_NUTRITION);
    }

    @Tool(name = GET_INGREDIENT_NUTRITION, description = "按食材名称查询营养数据")
    public ToolResult<NutritionFacts> getIngredientNutrition(
            @ToolParam(name = "ingredient_name", description = "食材名称，如鸡胸肉") String ingredientName,
            @ToolParam(name = "amount", description = "用量描述，如 100g") String amount) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return ToolResult.fail("INVALID_PARAM", "ingredient_name 不能为空");
        }
        // TODO 接入食材营养库
        return ToolResult.notImplemented(GET_INGREDIENT_NUTRITION);
    }

    @Tool(name = COMPARE_NUTRITION, description = "对比多道菜谱的营养数据，返回每道菜的营养对象列表")
    public ToolResult<List<NutritionFacts>> compareNutrition(
            @ToolParam(name = "recipe_ids", description = "要对比的菜谱ID列表") List<Long> recipeIds,
            @ToolParam(name = "servings", description = "每道按几人份计算，默认 1") Integer servings) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            return ToolResult.fail("INVALID_PARAM", "recipe_ids 不能为空");
        }
        // TODO 批量查营养库后原样返回，比较结论由 Agent 做
        return ToolResult.notImplemented(COMPARE_NUTRITION);
    }
}
