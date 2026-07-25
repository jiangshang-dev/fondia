package org.jeecg.modules.ai.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 绑定 material.ai.*：意图路由关键词 + 可扩展 MCP 清单。
 * 配置见 application-ai.yml。
 */
@Data
@Component
@ConfigurationProperties(prefix = "agentscope.ai")
public class AgentScopeAiProperties {

    private IntentConfig intent = new IntentConfig();

    private List<McpServerConfig> mcpServers = new ArrayList<>();

    @Data
    public static class IntentConfig {
        /** 是否启用意图路由；关闭则始终走 fallbackIntent */
        private boolean enabled = true;
        /** 规则未命中时是否调用模型做 JSON 分类 */
        private boolean useLlm = false;
        /** 分类失败时的默认意图 */
        private String fallbackIntent = "chat";
        /** intent -> 关键词列表（规则路由） */
        private Map<String, List<String>> keywords = new LinkedHashMap<>();
    }

    @Data
    public static class McpServerConfig {
        private String id;
        private String name;
        /** 命中任一意图时激活该 MCP 工具组 */
        private List<String> intents = new ArrayList<>();
        private String url;
        /** sse | streamable_http */
        private String transport = "sse";
        private boolean enabled = true;
        private Map<String, String> headers = new LinkedHashMap<>();
        private long timeoutSeconds = 60;
    }
}
