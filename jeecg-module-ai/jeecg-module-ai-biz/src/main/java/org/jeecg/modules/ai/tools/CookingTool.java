package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.CookingGuide;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.springframework.stereotype.Component;

import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_COOKING_METHOD;

/**
 * 烹饪领域 Tool：取出库里的标准做法。空气炸锅改法、少辣调整由 cooking-agent 基于详情推理。
 */
@Component
public class CookingTool {

    @Tool(name = GET_COOKING_METHOD, description = "按菜谱ID查询标准烹饪步骤、时长和所需厨具，不负责改做法")
    public ToolResult<CookingGuide> getCookingMethod(
            @ToolParam(name = "recipe_id", description = "菜谱ID") Long recipeId) {
        if (recipeId == null) {
            return ToolResult.fail("INVALID_PARAM", "recipe_id 不能为空");
        }
        // TODO 接入菜谱步骤；改法不要做成 Tool
        return ToolResult.notImplemented(GET_COOKING_METHOD);
    }
}
