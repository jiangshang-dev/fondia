package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.RecipeCandidate;
import org.jeecg.modules.ai.tools.model.RecipeDetailResult;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_RECIPE_DETAIL;
import static org.jeecg.modules.ai.tools.FondiaToolNames.RECOMMEND_RECIPE;
import static org.jeecg.modules.ai.tools.FondiaToolNames.SEARCH_RECIPE;

/**
 * 菜谱领域 Tool：只查候选和详情，不判断「能不能吃」。
 * 「适不适合减脂 / 能不能吃」由 Agent 结合营养数据和用户画像完成。
 */
@Component
public class RecipeTool {

    @Tool(name = SEARCH_RECIPE, description = "按关键词、食材、菜系、烹饪时间搜索菜谱候选，返回菜谱列表，不负责最终推荐判断")
    public ToolResult<List<RecipeCandidate>> searchRecipe(
            @ToolParam(name = "keyword", description = "菜名或关键词，如宫保鸡丁、川菜") String keyword,
            @ToolParam(name = "ingredients", description = "已有食材列表，如鸡胸肉、西兰花") List<String> ingredients,
            @ToolParam(name = "cuisine", description = "菜系，如川菜、粤菜") String cuisine,
            @ToolParam(name = "max_cook_minutes", description = "最长烹饪时间（分钟）") Integer maxCookMinutes,
            @ToolParam(name = "limit", description = "返回条数，默认 5") Integer limit) {
        // TODO 接入 search / cuisine 服务（ES + MySQL）
        return ToolResult.notImplemented(SEARCH_RECIPE);
    }

    @Tool(name = GET_RECIPE_DETAIL, description = "按菜谱ID或菜名查询单道菜谱详情：食材、步骤、时长。优先使用 recipe_id")
    public ToolResult<RecipeDetailResult> getRecipeDetail(
            @ToolParam(name = "recipe_id", description = "菜谱ID，有则优先") Long recipeId,
            @ToolParam(name = "recipe_name", description = "菜谱名称，无 ID 时使用") String recipeName) {
        if (recipeId == null && (recipeName == null || recipeName.isBlank())) {
            return ToolResult.fail("INVALID_PARAM", "recipe_id 与 recipe_name 至少提供一个");
        }
        // TODO 接入 IFdRecipeService.getRecipeDetail
        return ToolResult.notImplemented(GET_RECIPE_DETAIL);
    }

    @Tool(name = RECOMMEND_RECIPE, description = "按食材、人数、口味、时长、饮食目标从数据库筛选菜谱候选。只返回候选列表，由 Agent 决定最终推荐哪几道")
    public ToolResult<List<RecipeCandidate>> recommendRecipe(
            @ToolParam(name = "ingredients", description = "已有食材") List<String> ingredients,
            @ToolParam(name = "people", description = "用餐人数") Integer people,
            @ToolParam(name = "taste", description = "口味，如清淡、微辣") String taste,
            @ToolParam(name = "max_cook_minutes", description = "最长烹饪时间（分钟）") Integer maxCookMinutes,
            @ToolParam(name = "goal", description = "饮食目标：减脂/增肌/控糖/家常/健康") String goal,
            @ToolParam(name = "limit", description = "返回条数，默认 5") Integer limit) {
        // TODO 按条件检索候选，不要在 Tool 内做「适不适合吃」的自然语言建议
        return ToolResult.notImplemented(RECOMMEND_RECIPE);
    }
}
