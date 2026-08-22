package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.jeecg.modules.ai.tools.model.ToolResult;
import org.jeecg.modules.ai.tools.model.WeatherSnapshot;
import org.springframework.stereotype.Component;

import static org.jeecg.modules.ai.tools.FondiaToolNames.GET_WEATHER;

@Component
public class WeatherTool {

    @Tool(name = GET_WEATHER, description = "获取城市当前天气概况")
    public ToolResult<WeatherSnapshot> getWeather(
            @ToolParam(name = "city", description = "城市名称") String city) {
        if (city == null || city.isBlank()) {
            return ToolResult.fail("INVALID_PARAM", "city 不能为空");
        }
        // 演示数据，后续接天气服务
        return ToolResult.ok(WeatherSnapshot.builder()
                .city(city)
                .condition("晴")
                .temperatureC(24)
                .build());
    }
}
