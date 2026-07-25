package org.jeecg.modules.ai.service;

import org.jeecg.modules.ai.config.McpToolCatalog;
import org.jeecg.modules.ai.config.McpToolCatalog.McpToolItem;
import org.jeecg.modules.ai.config.McpToolCatalog.ServerStatus;
import org.jeecg.modules.ai.dto.ChatDtos.ApiResponse;
import org.jeecg.modules.ai.dto.ToolDtos.AgentToolVo;
import org.jeecg.modules.ai.dto.ToolDtos.McpStatusVo;
import org.jeecg.modules.ai.dto.ToolDtos.ToolListResult;
import io.agentscope.core.model.ToolSchema;
import io.agentscope.harness.agent.HarnessAgent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final HarnessAgent harnessAgent;
    private final McpToolCatalog mcpToolCatalog;

    public ToolListResult listTools() {
        Set<String> mcpNames = new HashSet<>();
        List<AgentToolVo> mcpTools = new ArrayList<>();
        for (McpToolItem item : mcpToolCatalog.getAllTools()) {
            mcpNames.add(item.getName());
            mcpTools.add(
                    AgentToolVo.builder()
                            .name(item.getName())
                            .description(item.getDescription())
                            .source("mcp")
                            .serverId(item.getServerId())
                            .build());
        }

        List<McpStatusVo> mcpServers = new ArrayList<>();
        for (ServerStatus server : mcpToolCatalog.getServers()) {
            mcpServers.add(
                    McpStatusVo.builder()
                            .connected(server.isConnected())
                            .clientName(server.getName())
                            .url(server.getUrl())
                            .errorMessage(server.getErrorMessage())
                            .toolCount(server.getTools().size())
                            .serverId(server.getId())
                            .intents(server.getIntents())
                            .build());
        }

        List<String> activeGroups = harnessAgent.getToolkit().getActiveGroups();
        List<AgentToolVo> agentTools = new ArrayList<>();
        List<ToolSchema> schemas =
                activeGroups == null || activeGroups.isEmpty()
                        ? harnessAgent.getToolkit().getToolSchemas()
                        : harnessAgent.getToolkit().getToolSchemas(activeGroups);
        for (ToolSchema schema : schemas) {
            String source = mcpNames.contains(schema.getName()) ? "mcp" : "agent";
            agentTools.add(
                    AgentToolVo.builder()
                            .name(schema.getName())
                            .description(schema.getDescription())
                            .source(source)
                            .build());
        }

        McpStatusVo summary =
                McpStatusVo.builder()
                        .connected(mcpToolCatalog.hasAnyConnected())
                        .clientName(mcpToolCatalog.getClientName())
                        .url(mcpToolCatalog.getUrl())
                        .errorMessage(mcpToolCatalog.getErrorMessage())
                        .toolCount(mcpTools.size())
                        .build();

        return ToolListResult.builder()
                .mcp(summary)
                .mcpServers(mcpServers)
                .mcpTools(mcpTools)
                .agentTools(agentTools)
                .activeGroups(activeGroups)
                .totalAgentTools(agentTools.size())
                .build();
    }

    public ApiResponse<ToolListResult> listToolsResponse() {
        return ApiResponse.ok(listTools());
    }
}
