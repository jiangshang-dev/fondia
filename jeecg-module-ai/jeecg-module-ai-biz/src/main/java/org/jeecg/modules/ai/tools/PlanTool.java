package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.MealPlanRecord;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_MEAL_PLAN;
import static org.jeecg.modules.ai.tools.FondiaToolNames.SAVE_MEAL_PLAN;

/**
 * 饮食计划持久化 Tool。菜单怎么排由 plan-agent 推理，这里只负责存取。
 */
@Component
public class PlanTool {

    @Tool(name = SAVE_MEAL_PLAN, description = "保存用户饮食计划。meals 为按天的早午晚菜谱ID结构，由 Agent 编排后再入库")
    public ToolResult<MealPlanRecord> saveMealPlan(
            @ToolParam(name = "title", description = "计划标题，如本周晚餐") String title,
            @ToolParam(name = "days", description = "天数") Integer days,
            @ToolParam(name = "start_date", description = "开始日期 yyyy-MM-dd") String startDate,
            @ToolParam(name = "meals", description = "每日餐次对应的菜谱ID列表") List<MealPlanRecord.MealPlanDay> meals) {
        if (title == null || title.isBlank()) {
            return ToolResult.fail("INVALID_PARAM", "title 不能为空");
        }
        // TODO 接入计划表
        return ToolResult.notImplemented(SAVE_MEAL_PLAN);
    }

    @Tool(name = GET_MEAL_PLAN, description = "查询用户已保存的饮食计划；不传 plan_id 则返回当前有效计划")
    public ToolResult<MealPlanRecord> getMealPlan(
            @ToolParam(name = "plan_id", description = "计划ID，可选") Long planId) {
        // TODO 接入计划表
        return ToolResult.notImplemented(GET_MEAL_PLAN);
    }
}
