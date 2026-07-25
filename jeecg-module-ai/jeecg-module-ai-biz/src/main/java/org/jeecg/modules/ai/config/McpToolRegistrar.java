package org.jeecg.modules.ai.config;

import org.jeecg.modules.ai.constant.IntentType;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolGroupScope;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.agentscope.harness.agent.HarnessAgent;
import io.modelcontextprotocol.spec.McpSchema;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * 启动后按 material.ai.mcp-servers 注册多个 MCP。
 * 每个 MCP 放入独立 ToolGroup（EXTERNAL），默认不激活；
 * 对话时由意图路由激活对应 group。
 * <p>
 * ModelScope 的 /mcp 是 Streamable HTTP：POST 正常，GET 会返回 JSON 而非 SSE。
 * MCP Java SDK 在 initialize 后会额外 GET 一次做 out-of-band 流，可能打出
 * {@code Invalid SSE response} 日志，只要 listTools 成功即可忽略。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolRegistrar implements SmartInitializingSingleton {

    private final HarnessAgent harnessAgent;
    private final McpToolCatalog mcpToolCatalog;
    private final AgentScopeAiProperties AgentScopeAiProperties;

    private final Set<String> registeredGroupIds = Collections.synchronizedSet(new LinkedHashSet<>());

    @Override
    public void afterSingletonsInstantiated() {
        mcpToolCatalog.clear();
        registeredGroupIds.clear();

        Toolkit agentToolkit = harnessAgent.getToolkit();
        List<AgentScopeAiProperties.McpServerConfig> servers = resolveMcpServers();
        if (servers.isEmpty()) {
            log.warn("未配置任何 MCP 服务，跳过注册");
            return;
        }

        for (AgentScopeAiProperties.McpServerConfig cfg : servers) {
            registerOne(agentToolkit, cfg);
        }

        printAgentTools(agentToolkit);
        log.info(
                "MCP 注册完成：成功 {}/{} 个，groups={}",
                registeredGroupIds.size(),
                servers.stream().filter(s -> s != null && s.isEnabled()).count(),
                registeredGroupIds);
    }

    public Set<String> getRegisteredGroupIds() {
        return Set.copyOf(registeredGroupIds);
    }

    public List<String> resolveActiveGroups(Set<String> intents) {
        Set<String> effective =
                intents == null || intents.isEmpty()
                        ? Set.of(blankToDefault(
                                AgentScopeAiProperties.getIntent().getFallbackIntent(),
                                IntentType.CHAT))
                        : intents;

        List<String> groups = new ArrayList<>();
        for (AgentScopeAiProperties.McpServerConfig cfg : resolveMcpServers()) {
            if (cfg == null || !cfg.isEnabled()) {
                continue;
            }
            String groupId = blankToDefault(cfg.getId(), cfg.getName());
            if (!registeredGroupIds.contains(groupId)) {
                continue;
            }
            List<String> serverIntents =
                    cfg.getIntents() == null ? List.of() : cfg.getIntents();
            if (serverIntents.isEmpty() || !Collections.disjoint(serverIntents, effective)) {
                groups.add(groupId);
            }
        }
        return groups;
    }

    private void registerOne(Toolkit toolkit, AgentScopeAiProperties.McpServerConfig cfg) {
        if (cfg == null || !cfg.isEnabled()) {
            return;
        }
        String groupId = blankToDefault(cfg.getId(), cfg.getName());
        if (isBlank(cfg.getUrl())) {
            markFailure(groupId, cfg, "url 为空");
            return;
        }

        String transport = resolveTransport(cfg);
        try {
            // 先做轻量探测，避免对已下线 ModelScope 记录反复抛 SSE 异常
            String probeError = probeEndpoint(cfg, transport);
            if (probeError != null) {
                markFailure(groupId, cfg, probeError);
                log.warn("跳过 MCP id={} url={} reason={}", groupId, cfg.getUrl(), probeError);
                return;
            }

            McpClientWrapper client = buildMcpClient(cfg, transport);
            if (client == null) {
                markFailure(groupId, cfg, "客户端构建失败或 Authorization 无效");
                return;
            }

            Duration timeout = Duration.ofSeconds(
                    cfg.getTimeoutSeconds() > 0 ? cfg.getTimeoutSeconds() : 60);

            // 与 ai-service 一致：交给 Toolkit 完成 initialize + listTools
            // （SDK 随后 GET SSE 失败只会打日志，不阻断注册）
            toolkit.createToolGroup(
                    groupId,
                    "MCP:" + blankToDefault(cfg.getName(), groupId),
                    false,
                    ToolGroupScope.EXTERNAL);

            toolkit.registration()
                    .mcpClient(client)
                    .group(groupId)
                    .apply();

            List<McpSchema.Tool> remoteTools = safeListTools(client, timeout);
            List<McpToolCatalog.McpToolItem> catalogItems = toCatalogItems(groupId, remoteTools);

            registeredGroupIds.add(groupId);
            mcpToolCatalog.addServer(
                    new McpToolCatalog.ServerStatus(
                            groupId,
                            cfg.getName(),
                            cfg.getUrl(),
                            true,
                            null,
                            cfg.getIntents(),
                            catalogItems));

            log.info(
                    "已注册 MCP group={} transport={} tools={} intents={}",
                    groupId,
                    transport,
                    catalogItems.size(),
                    cfg.getIntents());
            printMcpTools(groupId, catalogItems);
        } catch (Exception e) {
            markFailure(groupId, cfg, e.getMessage());
            log.warn(
                    "MCP 注册失败（已跳过，不影响启动）id={} transport={} url={}: {}",
                    groupId,
                    transport,
                    cfg.getUrl(),
                    e.getMessage());
        }
    }

    /**
     * URL 含 /sse → sse；含 /mcp 或配置为 streamable_http → streamable_http。
     * 避免误用 SSE 去 GET /mcp，从而出现 Invalid SSE response。
     */
    private String resolveTransport(AgentScopeAiProperties.McpServerConfig cfg) {
        String configured =
                blankToDefault(cfg.getTransport(), "").trim().toLowerCase(Locale.ROOT);
        String url = blankToDefault(cfg.getUrl(), "").toLowerCase(Locale.ROOT);

        if (url.contains("/sse")) {
            return "sse";
        }
        if (url.contains("/mcp")
                || "streamable_http".equals(configured)
                || "streamable-http".equals(configured)
                || "http".equals(configured)) {
            return "streamable_http";
        }
        if (!configured.isBlank()) {
            return configured;
        }
        return "sse";
    }

    /**
     * @return null 表示可继续注册；否则返回跳过原因
     */
    private String probeEndpoint(AgentScopeAiProperties.McpServerConfig cfg, String transport) {
        if (!"streamable_http".equals(transport)) {
            return null;
        }
        try {
            Duration timeout = Duration.ofSeconds(
                    Math.min(cfg.getTimeoutSeconds() > 0 ? cfg.getTimeoutSeconds() : 15, 15));
            HttpClient client =
                    HttpClient.newBuilder()
                            .connectTimeout(timeout)
                            .version(HttpClient.Version.HTTP_1_1)
                            .build();

            String body =
                    """
                    {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"jeecg-ai-probe","version":"1.0"}}}
                    """;
            HttpRequest.Builder req =
                    HttpRequest.newBuilder(URI.create(cfg.getUrl()))
                            .timeout(timeout)
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json, text/event-stream")
                            .POST(HttpRequest.BodyPublishers.ofString(body));

            Map<String, String> headers = cfg.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> h : headers.entrySet()) {
                    if (!isBlank(h.getKey()) && !isBlank(h.getValue()) && !h.getValue().contains("${")) {
                        req.header(h.getKey(), h.getValue());
                    }
                }
            }

            HttpResponse<String> resp = client.send(req.build(), HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String respBody = resp.body() == null ? "" : resp.body();
            if (code == 404 || respBody.contains("record not found")) {
                return "远端 MCP 记录不存在(404)，请到 ModelScope 重新部署或关闭 enabled";
            }
            if (code < 200 || code >= 300) {
                return "探测失败 HTTP " + code + ": " + abbreviate(respBody, 120);
            }
            return null;
        } catch (Exception e) {
            return "探测超时或网络失败: " + e.getMessage();
        }
    }

    private McpClientWrapper buildMcpClient(
            AgentScopeAiProperties.McpServerConfig cfg, String transport) {
        String name = blankToDefault(cfg.getName(), blankToDefault(cfg.getId(), "mcp"));
        Duration timeout =
                Duration.ofSeconds(cfg.getTimeoutSeconds() > 0 ? cfg.getTimeoutSeconds() : 60);

        McpClientBuilder builder =
                McpClientBuilder.create(name)
                        .timeout(timeout)
                        .protocolVersions("2024-11-05", "2025-03-26");

        if ("streamable_http".equals(transport)
                || "streamable-http".equals(transport)
                || "http".equals(transport)) {
            builder.streamableHttpTransport(cfg.getUrl())
                    .customizeStreamableHttpClient(
                            http -> http.version(HttpClient.Version.HTTP_1_1));
        } else {
            builder.sseTransport(cfg.getUrl())
                    .customizeSseClient(http -> http.version(HttpClient.Version.HTTP_1_1));
        }

        Map<String, String> headers = cfg.getHeaders();
        if (headers != null) {
            if (headers.containsKey("Authorization")
                    && !isUsableAuthHeader(headers.get("Authorization"))) {
                log.warn("MCP Authorization 无效，跳过注册: id={}", cfg.getId());
                return null;
            }
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (isBlank(header.getKey()) || isBlank(header.getValue())) {
                    continue;
                }
                if (header.getValue().contains("${")) {
                    log.warn(
                            "MCP header 未解析，跳过注册: id={}, header={}",
                            cfg.getId(),
                            header.getKey());
                    return null;
                }
                builder.header(header.getKey(), header.getValue());
            }
        }

        return builder.buildAsync().block(timeout);
    }

    private List<AgentScopeAiProperties.McpServerConfig> resolveMcpServers() {
        List<AgentScopeAiProperties.McpServerConfig> configured =
                AgentScopeAiProperties.getMcpServers();
        if (configured != null && !configured.isEmpty()) {
            return configured;
        }
        return List.of();
    }

    private void markFailure(
            String groupId, AgentScopeAiProperties.McpServerConfig cfg, String error) {
        mcpToolCatalog.addServer(
                new McpToolCatalog.ServerStatus(
                        groupId,
                        cfg.getName(),
                        cfg.getUrl(),
                        false,
                        error,
                        cfg.getIntents(),
                        List.of()));
    }

    private List<McpSchema.Tool> safeListTools(McpClientWrapper client, Duration timeout) {
        try {
            List<McpSchema.Tool> tools = client.listTools().block(timeout);
            return tools == null ? List.of() : tools;
        } catch (Exception e) {
            log.warn("listTools 失败，仍保留已注册 MCP: {}", e.getMessage());
            return List.of();
        }
    }

    private List<McpToolCatalog.McpToolItem> toCatalogItems(
            String serverId, List<McpSchema.Tool> remoteTools) {
        if (remoteTools == null || remoteTools.isEmpty()) {
            return List.of();
        }
        List<McpToolCatalog.McpToolItem> items = new ArrayList<>();
        for (McpSchema.Tool tool : remoteTools) {
            items.add(
                    new McpToolCatalog.McpToolItem(
                            tool.name(),
                            tool.description() != null ? tool.description() : "",
                            serverId));
        }
        return items;
    }

    private void printMcpTools(String groupId, List<McpToolCatalog.McpToolItem> tools) {
        log.info("========== MCP[{}] 工具列表 ({} 个) ==========", groupId, tools.size());
        for (int i = 0; i < tools.size(); i++) {
            McpToolCatalog.McpToolItem tool = tools.get(i);
            log.info(
                    "[MCP {}] {} — {}",
                    i + 1,
                    tool.getName(),
                    abbreviate(tool.getDescription(), 120));
        }
    }

    private void printAgentTools(Toolkit toolkit) {
        var schemas = toolkit.getToolSchemas();
        log.info("当前可见工具（仅已激活 group + 未分组）共 {} 个", schemas.size());
        schemas.forEach(
                schema ->
                        log.info(
                                "[Agent Tool visible] {} — {}",
                                schema.getName(),
                                abbreviate(schema.getDescription(), 120)));
    }

    private static boolean isUsableAuthHeader(String authorization) {
        if (isBlank(authorization) || authorization.contains("${")) {
            return false;
        }
        String token = authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        return !token.isBlank();
    }

    private static String blankToDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String abbreviate(String text, int maxLen) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.replace('\n', ' ').trim();
        return normalized.length() <= maxLen ? normalized : normalized.substring(0, maxLen) + "…";
    }
}
