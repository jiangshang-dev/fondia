package org.jeecg.modules.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * 天气工具
 */
public class WeatherTool {

    @Tool(name = "getWeather", description = "获取城市的天气")
    public String getWeather(@ToolParam(name = "city", description = "城市名称") String city) {
        return "今天" + city + "天气晴朗";
    }
}
