package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.MealHistoryItem;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.jeecg.modules.ai.tools.model.UserDietProfile;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_MEAL_HISTORY;
import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_USER_PROFILE;

/**
 * 用户上下文 Tool：画像、忌口、近期饮食。不做成独立 Agent。
 */
@Component
public class UserContextTool {

    @Tool(name = GET_USER_PROFILE, description = "查询当前用户饮食画像：口味、忌口、饮食偏好、厨具、厨艺等级。不含「能不能吃」的建议")
    public ToolResult<UserDietProfile> getUserProfile() {
        // TODO 接入用户服务 dietaryPreference / tastePreference，userId 从会话透传
        return ToolResult.notImplemented(GET_USER_PROFILE);
    }

    @Tool(name = GET_MEAL_HISTORY, description = "查询用户最近吃过/浏览/收藏的菜谱，用于降低重复推荐")
    public ToolResult<List<MealHistoryItem>> getMealHistory(
            @ToolParam(name = "days", description = "回溯天数，默认 7") Integer days) {
        // TODO 接入浏览/收藏/饮食记录
        return ToolResult.notImplemented(GET_MEAL_HISTORY);
    }
}
