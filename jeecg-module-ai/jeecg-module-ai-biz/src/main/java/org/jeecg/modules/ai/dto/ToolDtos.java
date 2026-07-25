package org.jeecg.modules.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class ToolDtos {

    private ToolDtos() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentToolVo {
        private String name;
        private String description;
        /** mcp | agent */
        private String source;
        /** MCP server id / tool group id */
        private String serverId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpStatusVo {
        private boolean connected;
        private String clientName;
        private String url;
        private String errorMessage;
        private int toolCount;
        private String serverId;
        private List<String> intents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolListResult {
        /** 兼容旧字段：汇总视图 */
        private McpStatusVo mcp;
        private List<McpStatusVo> mcpServers;
        private List<AgentToolVo> mcpTools;
        private List<AgentToolVo> agentTools;
        private List<String> activeGroups;
        private int totalAgentTools;
    }
}
