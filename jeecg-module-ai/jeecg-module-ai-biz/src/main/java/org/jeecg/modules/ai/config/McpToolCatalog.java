package org.jeecg.modules.ai.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 记录多 MCP 服务注册状态与工具清单，供 /api/tools 查询。
 */
@Component
@Data
public class McpToolCatalog {

    private final List<ServerStatus> servers = new CopyOnWriteArrayList<>();

    @Data
    public static class McpToolItem {
        private final String name;
        private final String description;
        private final String serverId;
        private final String source;

        public McpToolItem(String name, String description, String serverId) {
            this.name = name;
            this.description = description;
            this.serverId = serverId;
            this.source = "mcp";
        }
    }

    @Data
    public static class ServerStatus {
        private final String id;
        private final String name;
        private final String url;
        private final boolean connected;
        private final String errorMessage;
        private final List<String> intents;
        private final List<McpToolItem> tools;

        public ServerStatus(String id, String name, String url, boolean connected,
                String errorMessage, List<String> intents, List<McpToolItem> tools) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.connected = connected;
            this.errorMessage = errorMessage;
            this.intents = intents == null ? List.of() : List.copyOf(intents);
            this.tools = tools == null ? List.of() : List.copyOf(tools);
        }
    }

    public void clear() {
        servers.clear();
    }

    public void addServer(ServerStatus status) {
        servers.add(status);
    }

    public List<ServerStatus> getServers() {
        return Collections.unmodifiableList(servers);
    }

    public List<McpToolItem> getAllTools() {
        List<McpToolItem> all = new ArrayList<>();
        for (ServerStatus server : servers) {
            all.addAll(server.getTools());
        }
        return all;
    }

    public boolean hasAnyConnected() {
        return servers.stream().anyMatch(ServerStatus::isConnected);
    }

    /** 兼容旧单 MCP 视图 */
    public boolean isConnected() {
        return hasAnyConnected();
    }

    public String getClientName() {
        return servers.stream().filter(ServerStatus::isConnected)
                .map(ServerStatus::getName)
                .findFirst()
                .orElse(null);
    }

    public String getUrl() {
        return servers.stream().filter(ServerStatus::isConnected)
                .map(ServerStatus::getUrl)
                .findFirst()
                .orElse(servers.isEmpty() ? null : servers.get(0).getUrl());
    }

    public String getErrorMessage() {
        return servers.stream()
                .filter(s -> !s.isConnected() && s.getErrorMessage() != null)
                .map(ServerStatus::getErrorMessage)
                .findFirst()
                .orElse(null);
    }

    public List<McpToolItem> getMcpTools() {
        return getAllTools();
    }
}
