package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.IngredientItem;
import org.jeecg.modules.ai.tools.model.InventoryItem;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_EXPIRING_INGREDIENTS;
import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_INVENTORY;
import static org.jeecg.modules.ai.tools.FondiaToolNames.SEARCH_INGREDIENT;

/**
 * 食材领域 Tool：识别、库存、临期。不负责最终点哪几道菜。
 */
@Component
public class IngredientTool {

    @Tool(name = SEARCH_INGREDIENT, description = "把用户提到的食材名称规范化，并查询是否在食材库中及分类")
    public ToolResult<List<IngredientItem>> searchIngredient(
            @ToolParam(name = "ingredients", description = "用户提到的食材，如西红柿、鸡胸肉") List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return ToolResult.fail("INVALID_PARAM", "ingredients 不能为空");
        }
        // TODO 接入食材主数据 / 同义词
        return ToolResult.notImplemented(SEARCH_INGREDIENT);
    }

    @Tool(name = GET_INVENTORY, description = "查询当前用户冰箱/库存中的食材及用量")
    public ToolResult<List<InventoryItem>> getInventory() {
        // TODO 接入用户库存服务，userId 从 RuntimeContext 透传
        return ToolResult.notImplemented(GET_INVENTORY);
    }

    @Tool(name = GET_EXPIRING_INGREDIENTS, description = "查询即将过期、应优先消耗的库存食材")
    public ToolResult<List<InventoryItem>> getExpiringIngredients(
            @ToolParam(name = "within_days", description = "多少天内过期，默认 3") Integer withinDays) {
        // TODO 接入库存保质期
        return ToolResult.notImplemented(GET_EXPIRING_INGREDIENTS);
    }
}
