package org.jeecg.modules.ai.controller;

import org.jeecg.modules.ai.dto.ChatDtos.ApiResponse;
import org.jeecg.modules.ai.dto.ToolDtos.ToolListResult;
import org.jeecg.modules.ai.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent/ai")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /** 查看 MCP 工具及 Agent 当前可用工具列表 */
    @GetMapping("/tools")
    public ApiResponse<ToolListResult> listTools() {
        return toolService.listToolsResponse();
    }
}
