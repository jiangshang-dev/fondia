package org.jeecg.modules.ai.service;

import org.jeecg.modules.ai.config.McpToolRegistrar;
import org.jeecg.modules.ai.constant.IntentType;
import org.jeecg.modules.ai.router.IntentRouter;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 按意图激活 MCP ToolGroup，并写入 AgentState，保证本轮 stream 可见对应工具。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentMcpActivator {

    private static final String AGENT_STATE_KEY = "agent_state";

    private final IntentRouter intentRouter;
    private final McpToolRegistrar mcpToolRegistrar;
    private final HarnessAgent harnessAgent;
    private final AgentStateStore agentStateStore;

    /**
     * 识别意图并激活对应 MCP group。
     *
     * @param enableWebSearch true 时强制附加 web_search 意图（联网搜索开关）
     * @return 激活结果（意图 + group），便于 SSE 透出
     */
    public ActivationResult activate(
            String userId, String sessionId, String message, boolean enableWebSearch) {
        Set<String> intents = new LinkedHashSet<>(intentRouter.route(message, userId));
        if (enableWebSearch) {
            intents.add(IntentType.WEB_SEARCH);
            log.info("已开启联网搜索开关，强制附加 web_search 意图 userId={} sessionId={}",
                    userId,
                    sessionId);
        }
        List<String> groups = mcpToolRegistrar.resolveActiveGroups(intents);

        Toolkit toolkit = harnessAgent.getToolkit();
        // 共享 Toolkit：本 Demo 单用户场景下直接切换 active groups
        toolkit.setActiveGroups(new ArrayList<>(groups));

        AgentState state = harnessAgent.getDelegate().getAgentState(userId, sessionId);
        if (state != null && state.getToolContext() != null) {
            state.getToolContext().setActivatedGroups(new ArrayList<>(groups));
            try {
                agentStateStore.save(userId, sessionId, AGENT_STATE_KEY, state);
            } catch (Exception e) {
                log.warn("写入意图激活 group 到 Redis 失败 userId={} sessionId={}: {}",
                        userId,
                        sessionId,
                        e.getMessage());
            }
        }

        log.info("意图激活 MCP: userId={} sessionId={} enableWebSearch={} intents={} groups={}",
                userId,
                sessionId,
                enableWebSearch,
                intents,
                groups);
        return new ActivationResult(
                List.copyOf(intents), List.copyOf(groups), enableWebSearch);
    }

    /** 兼容旧调用 */
    public ActivationResult activate(String userId, String sessionId, String message) {
        return activate(userId, sessionId, message, false);
    }

    public record ActivationResult(
            List<String> intents, List<String> activeGroups, boolean enableWebSearch) {}
}
