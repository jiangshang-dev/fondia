package org.jeecg.modules.ai.tools;

import java.util.List;

/**
 * 业务 Tool 名称与子 Agent 白名单。名称必须与 {@code @Tool(name=...)} 一致。
 */
public final class FondiaToolNames {

    public static final String SEARCH_RECIPE = "search_recipe";
    public static final String GET_RECIPE_DETAIL = "get_recipe_detail";
    public static final String RECOMMEND_RECIPE = "recommend_recipe";

    public static final String SEARCH_INGREDIENT = "search_ingredient";
    public static final String GET_INVENTORY = "get_inventory";
    public static final String GET_EXPIRING_INGREDIENTS = "get_expiring_ingredients";

    public static final String GET_RECIPE_NUTRITION = "get_recipe_nutrition";
    public static final String GET_INGREDIENT_NUTRITION = "get_ingredient_nutrition";
    public static final String COMPARE_NUTRITION = "compare_nutrition";

    public static final String GET_USER_PROFILE = "get_user_profile";
    public static final String GET_MEAL_HISTORY = "get_meal_history";

    public static final String SAVE_MEAL_PLAN = "save_meal_plan";
    public static final String GET_MEAL_PLAN = "get_meal_plan";

    public static final String GET_COOKING_METHOD = "get_cooking_method";

    public static final String GET_WEATHER = "getWeather";

    private FondiaToolNames() {
    }

    public static List<String> menuAgent() {
        return List.of(
                SEARCH_RECIPE,// 搜索菜谱
                GET_RECIPE_DETAIL, // 获取菜谱详情
                RECOMMEND_RECIPE // 推荐菜谱
        );
    }

    /**
     * 食材分析agent
     * @return
     */
    public static List<String> ingredientAgent() {
        return List.of(
                SEARCH_INGREDIENT, // 搜索食材
                GET_INVENTORY, // 获取用户库存
                GET_EXPIRING_INGREDIENTS // 获取过期食材
        );
    }

    /**
     * 营养分析agent
     * @return
     */
    public static List<String> nutritionAgent() {
        return List.of(
                GET_RECIPE_NUTRITION, // 获取菜谱营养信息
                GET_INGREDIENT_NUTRITION, // 获取食材营养信息
                COMPARE_NUTRITION // 对比营养信息
        );
    }

    /**
     * 饮食计划agent
     */
    public static List<String> planAgent() {
        return List.of(
                GET_USER_PROFILE, // 获取用户个人信息
                GET_MEAL_HISTORY, // 获取用户饮食历史
                GET_INVENTORY, // 获取用户库存
                SEARCH_RECIPE, // 搜索菜谱
                SAVE_MEAL_PLAN, // 保存用户饮食计划
                GET_MEAL_PLAN // 获取用户饮食计划
        );
    }

    /**
     * 烹饪agent
     */
    public static List<String> cookingAgent() {
        return List.of(
                GET_RECIPE_DETAIL, // 获取菜谱详情
                GET_COOKING_METHOD // 获取烹饪方法
        );
    }
}
