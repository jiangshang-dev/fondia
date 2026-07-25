package org.jeecg.modules.ai.router;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.ai.config.AgentScopeAiProperties;
import org.jeecg.modules.ai.constant.IntentType;
import io.agentscope.core.model.Model;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.harness.agent.HarnessAgent;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图路由：规则优先，可选 LLM JSON 分类兜底。
 * 识别结果用于激活对应 MCP ToolGroup（group id = mcp-servers[].id）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentRouter {

    private static final Map<String, String> INTENT_HINTS = Map.of(
            IntentType.WEATHER, "天气/气温/降水/预报",
            IntentType.WEB_SEARCH, "联网搜索最新资讯/行情/新闻",
            IntentType.WEB_12306, "火车票/高铁票查询",
            IntentType.SUANMING, "命理排盘：四柱/八字排盘、干支、日主、十神、五行、喜用神、大运流年、紫微神煞；问自己命运/生辰批命",
            IntentType.HUANGLI, "黄历择日：今日宜忌、吉日凶日、冲煞、哪天适合结婚搬家开业；不问命盘只问日子好不好",
            IntentType.BAZI, "合婚/姻缘合盘/风水搬家运势（非单纯排盘或黄历宜忌）",
            IntentType.LOVE, "桃花/配偶星/情感测算",
            IntentType.CHAT, "闲聊或与上述无关");

    private final AgentScopeAiProperties AgentScopeAiProperties;
    private final Model chatModel;
    private final ObjectMapper objectMapper;

    public Set<String> route(String message, String classifyUserId) {
        AgentScopeAiProperties.IntentConfig intentConfig = AgentScopeAiProperties.getIntent();
        String fallback = blankToDefault(intentConfig.getFallbackIntent(), IntentType.CHAT);

        if (!intentConfig.isEnabled()) {
            return Set.of(fallback);
        }

        Set<String> ruleIntents = matchByKeywords(message, intentConfig.getKeywords());
        if (!ruleIntents.isEmpty()) {
            log.info("意图路由命中规则: messagePreview={}, intents={}", preview(message), ruleIntents);
            return normalize(ruleIntents, fallback);
        }

        if (intentConfig.isUseLlm() && chatModel != null) {
            Set<String> llmIntents = classifyByLlm(message, classifyUserId);
            if (!llmIntents.isEmpty()) {
                log.info("意图路由命中 LLM: messagePreview={}, intents={}", preview(message), llmIntents);
                return normalize(llmIntents, fallback);
            }
        }

        log.info("意图路由回落默认: messagePreview={}, fallback={}", preview(message), fallback);
        return Set.of(fallback);
    }

    private Set<String> matchByKeywords(String message, Map<String, List<String>> keywords) {
        if (message == null || message.isBlank() || keywords == null || keywords.isEmpty()) {
            return Collections.emptySet();
        }
        String text = message.toLowerCase(Locale.ROOT);
        Set<String> hit = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> entry : keywords.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            for (String keyword : entry.getValue()) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }
                if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                    hit.add(entry.getKey().trim());
                    break;
                }
            }
        }
        return hit;
    }

    private Set<String> classifyByLlm(String message, String classifyUserId) {
        try {
            log.debug("LLM 意图分类 userId={}", classifyUserId);
            HarnessAgent lite = HarnessAgent.builder()
                            .name("intent-router")
                            .model(chatModel)
                            .permissionContext(
                                    PermissionContextState.builder()
                                            .mode(PermissionMode.BYPASS)
                                            .build())
                            .build();
            var msg = lite.call(buildClassifyPrompt() + message).block();
            if (msg == null) {
                return Collections.emptySet();
            }
            return parseIntentJson(msg.getTextContent());
        } catch (Exception e) {
            log.warn("意图 LLM 分类失败，将回落默认意图: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    private String buildClassifyPrompt() {
        // 配置里的 keywords / mcp intents 与 IntentType 常量取并集，避免漏类
        Set<String> known = new LinkedHashSet<>(IntentType.classifyCandidates());
        Map<String, List<String>> keywords = AgentScopeAiProperties.getIntent().getKeywords();
        if (keywords != null) {
            known.addAll(keywords.keySet());
        }
        for (AgentScopeAiProperties.McpServerConfig server : AgentScopeAiProperties.getMcpServers()) {
            if (server != null && server.getIntents() != null) {
                known.addAll(server.getIntents());
            }
        }
        known.remove(IntentType.UNKNOWN);

        String intentDesc =
                known.stream()
                        .filter(s -> s != null && !s.isBlank())
                        .sorted()
                        .map(intent -> intent + "（" + INTENT_HINTS.getOrDefault(intent, "其他") + "）")
                        .collect(Collectors.joining("、"));

        return """
                你是意图分类器。根据用户最新一句话，从下列意图中选择（可多选）：
                %s。
                只输出 JSON，不要其它文字，格式：{"intents":["%s"],"reason":"简短原因"}
                用户消息：
                """
                .formatted(intentDesc, IntentType.CHAT);
    }

    Set<String> parseIntentJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptySet();
        }
        try {
            String json = extractJsonObject(raw);
            if (json == null || json.isBlank()) {
                return Collections.emptySet();
            }
            JsonNode root = objectMapper.readTree(json);
            JsonNode arr = root.get("intents");
            if (arr == null || !arr.isArray() || arr.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> intents = new LinkedHashSet<>();
            for (JsonNode item : arr) {
                if (item == null || item.isNull()) {
                    continue;
                }
                String intent = item.asText("").trim();
                if (!intent.isBlank()) {
                    intents.add(intent);
                }
            }
            return intents;
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private static String extractJsonObject(String raw) {
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstNl = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                text = text.substring(firstNl + 1, lastFence).trim();
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private Set<String> normalize(Set<String> intents, String fallback) {
        Set<String> result = new LinkedHashSet<>();
        for (String intent : intents) {
            if (intent == null || intent.isBlank() || IntentType.UNKNOWN.equals(intent)) {
                continue;
            }
            result.add(intent.trim());
        }
        if (result.isEmpty()) {
            result.add(fallback);
        }
        return result;
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String preview(String message) {
        if (message == null) {
            return "";
        }
        return message.length() <= 80 ? message : message.substring(0, 80) + "...";
    }
}
