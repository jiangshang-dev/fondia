package org.jeecg.modules.ai.service; // 声明当前类所在的包，便于 Spring 扫描与组织代码

// ========== 项目内依赖：配置、DTO、展示历史模型 ==========
import org.jeecg.modules.ai.config.AgentScopeDemoProperties; // 读取 application.yml 中的 workspace、agentName 等配置
import org.jeecg.modules.ai.dto.ChatDtos.ChatHistoryResult; // 历史查询 API 的返回体
import org.jeecg.modules.ai.dto.ChatDtos.ChatMessageVo; // 单条聊天消息（role + content）
import org.jeecg.modules.ai.dto.ChatDtos.SessionCreateResult; // 创建会话 API 的返回体
import org.jeecg.modules.ai.dto.ChatDtos.SessionVo; // 会话列表项
import org.jeecg.modules.ai.model.DisplayChatHistory; // Redis 中 UI 展示历史的持久化对象
import org.jeecg.modules.ai.service.IntentMcpActivator.ActivationResult;

// ========== Jackson：JSON 序列化 / 解析 JSONL ==========
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// ========== AgentScope 核心：Agent、消息、事件、权限、状态 ==========
import io.agentscope.core.ReActAgent; // 底层 ReAct Agent，用于 interrupt / saveAgentState
import io.agentscope.core.agent.RuntimeContext; // 携带 userId + sessionId，保证 Redis 键一致
import io.agentscope.core.event.AgentEvent; // 流式事件基类
import io.agentscope.core.event.AgentEventType; // 事件类型枚举（如 AGENT_END）
import io.agentscope.core.event.ConfirmResult; // HITL 工具确认结果
import io.agentscope.core.event.TextBlockDeltaEvent; // 流式文本增量事件
import io.agentscope.core.event.ThinkingBlockDeltaEvent; // 思考增量
import io.agentscope.core.event.ThinkingBlockEndEvent;
import io.agentscope.core.event.ThinkingBlockStartEvent;
import io.agentscope.core.event.ToolCallDeltaEvent; // 工具参数增量
import io.agentscope.core.event.ToolCallEndEvent;
import io.agentscope.core.event.ToolCallStartEvent; // 工具开始调用事件
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.event.ToolResultTextDeltaEvent;
import io.agentscope.core.message.AssistantMessage; // 构造 assistant 消息写入 context
import io.agentscope.core.message.Msg; // 消息基类
import io.agentscope.core.message.MsgRole; // USER / ASSISTANT / TOOL 等角色
import io.agentscope.core.message.ToolCallState; // 工具调用状态（ASKING 等）
import io.agentscope.core.message.ToolUseBlock; // 工具调用块
import io.agentscope.core.message.UserMessage; // 用户输入消息
import io.agentscope.core.permission.PermissionMode; // BYPASS：跳过 MCP 工具人工确认
import io.agentscope.core.state.AgentState; // Agent 内存状态（含 context 对话列表）
import io.agentscope.core.state.AgentStateStore; // 状态持久化接口（Redis 实现）
import io.agentscope.harness.agent.HarnessAgent; // AgentScope 2.0 封装 Agent，支持 streamEvents

// ========== JDK：IO、集合、并发、响应式 ==========
import java.io.BufferedReader; // 按行读取 JSONL 归档文件
import java.nio.charset.StandardCharsets; // 统一 UTF-8 编码
import java.nio.file.Files; // 判断文件是否存在
import java.nio.file.Path; // 拼接 workspace 下的 session 路径
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap; // SSE payload 保持字段顺序
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID; // 生成新 sessionId
import java.util.concurrent.ConcurrentHashMap; // 并发安全的流式缓冲 Map
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor; // 为 final 字段生成构造器（依赖注入）
import lombok.extern.slf4j.Slf4j; // 日志
import org.springframework.http.codec.ServerSentEvent; // SSE 响应格式
import org.springframework.stereotype.Service; // 标记为 Spring 业务 Bean
import reactor.core.publisher.Flux; // 流式返回 SSE 事件流

/**
 * 聊天核心业务类。
 * <p>职责：
 * <ul>
 *   <li>会话 CRUD、流式对话 SSE</li>
 *   <li>双轨历史：display_history（UI 永不压缩）+ agent_state（LLM 上下文）</li>
 *   <li>停止对话时保存已生成片段、自动确认 HITL 工具</li>
 * </ul>
 */
@Slf4j // 启用 log 变量
@Service // 注册为 Spring Service，供 Controller 注入
@RequiredArgsConstructor // 自动生成包含 harnessAgent 等 final 字段的构造器
public class ChatService {

    /** Redis 中 Agent LLM 上下文的 key，会被 AgentScope 读写，可能被 compaction 压缩 */
    private static final String AGENT_STATE_KEY = "agent_state";

    /** Redis 中 UI 展示历史的 key，只追加、不压缩，与 agent_state 分离 */
    private static final String DISPLAY_HISTORY_KEY = "display_history";

    /** 用户点「停止」后框架插入的英文占位句，展示历史时需过滤 */
    private static final String INTERRUPT_RECOVERY_SNIPPET =
            "I noticed that you have interrupted me";

    /** compaction 压缩后注入的摘要消息名，只给 LLM 看，不给用户看 */
    private static final String COMPACTION_SUMMARY_NAME = "__compaction_summary__";

    /** AgentScope HarnessAgent，提供 streamEvents / 权限 / 状态等能力 */
    private final HarnessAgent harnessAgent;

    /** Redis 状态存储，持久化 agent_state 与 display_history */
    private final AgentStateStore agentStateStore;

    /** JSON 序列化，用于 SSE data 与 JSONL 解析 */
    private final ObjectMapper objectMapper;

    /** 应用配置：workspace 路径、agent 名称、压缩开关等 */
    private final AgentScopeDemoProperties properties;

    /** 意图识别 → 激活对应 MCP ToolGroup */
    private final IntentMcpActivator intentMcpActivator;

    /**
     * 进行中的流式会话缓冲表。
     * key = userId/sessionId，value = 本轮已推送的文本增量。
     * 用于：停止对话时 Agent 来不及写 context，我们自行补存。
     */
    private final ConcurrentHashMap<String, StreamSessionBuffer> activeStreamBuffers =
            new ConcurrentHashMap<>();

    /**
     * 创建新会话，仅生成 sessionId，不写 Redis（首次发消息时才持久化）。
     *
     * @param userId 用户标识
     * @return 含 userId 与新 sessionId 的结果
     */
    public SessionCreateResult createSession(String userId) {
        String resolvedUserId = resolveUserId(userId); // 校验并 trim userId
        return SessionCreateResult.builder()
                .userId(resolvedUserId) // 返回规范化 userId
                .sessionId(UUID.randomUUID().toString()) // 全局唯一会话 ID
                .build();
    }

    /**
     * 列出某用户在 Redis 中已有会话 ID，并推导标题。
     */
    public List<SessionVo> listSessions(String userId) {
        String resolvedUserId = resolveUserId(userId);
        Set<String> sessionIds = agentStateStore.listSessionIds(resolvedUserId); // 从 Redis 索引读
        return sessionIds.stream()
                .map(
                        sessionId -> {
                            String title = deriveSessionTitle(resolvedUserId, sessionId); // 首条用户消息作标题
                            return SessionVo.builder()
                                    .userId(resolvedUserId)
                                    .sessionId(sessionId)
                                    .title(title)
                                    .updatedAt(System.currentTimeMillis()) // 列表排序用时间戳
                                    .build();
                        })
                .sorted(Comparator.comparing(SessionVo::getSessionId).reversed()) // 新 sessionId 通常更大，倒序
                .collect(Collectors.toList());
    }

    /** 查询历史，默认 source=auto（优先 display 展示历史） */
    public ChatHistoryResult getHistory(String userId, String sessionId) {
        return getHistory(userId, sessionId, "auto");
    }

    /**
     * 按数据源查询会话历史。
     *
     * @param source display=UI 历史；full=JSONL 原文；context=LLM 上下文；auto=优先 display
     */
    public ChatHistoryResult getHistory(String userId, String sessionId, String source) {
        String resolvedUserId = resolveUserId(userId); // 校验 userId
        requireSessionId(sessionId); // 校验 sessionId 非空
        String mode = normalizeHistorySource(source); // 规范化 source 参数

        // 读取 LLM 上下文（可能被 compaction）
        Optional<AgentState> stateOpt = agentStateStore.get(resolvedUserId, sessionId, AGENT_STATE_KEY, AgentState.class);
        boolean compacted = stateOpt.map(this::hasCompactionSummary).orElse(false); // 是否已压缩
        Path archiveFile = resolveSessionLogFile(resolvedUserId, sessionId); // workspace 下 JSONL 路径
        String archivePath = Files.isRegularFile(archiveFile) ? archiveFile.toString() : null;

        // 模式 full：读 JSONL 归档，raw=true 保留原文（含思考块）
        if ("full".equals(mode)) {
            return buildHistoryResult(
                    resolvedUserId,
                    sessionId,
                    loadMessagesFromSessionLog(archiveFile, true),
                    "full",
                    compacted,
                    archivePath,
                    stateOpt.isPresent() || Files.isRegularFile(archiveFile));
        }

        // 模式 context：直接读 Redis agent_state（调试用）
        if ("context".equals(mode)) {
            if (stateOpt.isEmpty()) {
                return emptyHistoryResult(resolvedUserId, sessionId, "context", compacted, archivePath);
            }
            return buildHistoryResult(
                    resolvedUserId,
                    sessionId,
                    toChatMessages(stateOpt.get(), true),
                    "context",
                    compacted,
                    archivePath,
                    true);
        }

        // 模式 display / auto：优先读永不压缩的 display_history
        List<ChatMessageVo> displayMessages = loadDisplayMessages(resolvedUserId, sessionId);
        if (displayMessages.isEmpty()) {
            // 旧会话无 display_history 时，从 JSONL 回填一次
            displayMessages = backfillDisplayHistoryFromLog(resolvedUserId, sessionId, archiveFile);
        }
        if (!displayMessages.isEmpty() || "display".equals(mode)) {
            return buildHistoryResult(
                    resolvedUserId,
                    sessionId,
                    displayMessages,
                    "display",
                    compacted,
                    archivePath,
                    !displayMessages.isEmpty() || stateOpt.isPresent());
        }

        // auto 兜底：display 为空则退回 agent_state
        if (stateOpt.isEmpty()) {
            return emptyHistoryResult(resolvedUserId, sessionId, "auto", compacted, archivePath);
        }
        return buildHistoryResult(
                resolvedUserId,
                sessionId,
                toChatMessages(stateOpt.get(), true),
                "context",
                compacted,
                archivePath,
                true);
    }

    /** 组装 ChatHistoryResult 响应体 */
    private ChatHistoryResult buildHistoryResult(
            String userId,
            String sessionId,
            List<ChatMessageVo> messages,
            String source,
            boolean compacted,
            String archivePath,
            boolean exists) {
        return ChatHistoryResult.builder()
                .userId(userId)
                .sessionId(sessionId)
                .exists(exists) // 会话是否存在
                .source(source) // 实际使用的数据源
                .compacted(compacted) // LLM 上下文是否已被压缩
                .archivePath(archivePath) // JSONL 归档路径（可能为 null）
                .messages(messages)
                .build();
    }

    /** 空历史结果（会话不存在或无消息） */
    private ChatHistoryResult emptyHistoryResult(
            String userId, String sessionId, String source, boolean compacted, String archivePath) {
        return ChatHistoryResult.builder()
                .userId(userId)
                .sessionId(sessionId)
                .exists(false)
                .source(source)
                .compacted(compacted)
                .archivePath(archivePath)
                .messages(List.of())
                .build();
    }

    /**
     * 流式对话：SSE 推送 Agent 事件，并在结束时持久化状态。
     * 发消息前先做意图识别，只激活命中意图对应的 MCP ToolGroup。
     * enableWebSearch=true 时强制附加 web_search 意图。
     */
    public Flux<ServerSentEvent<String>> streamChat(
            String userId, String sessionId, String message, boolean enableWebSearch) {
        String resolvedUserId = resolveUserId(userId);
        requireSessionId(sessionId);
        requireMessage(message);

        // 意图识别 → 激活对应 MCP（材料 / 天气 / 车票 / 天眼查 等）
        ActivationResult activation = intentMcpActivator.activate(resolvedUserId, sessionId, message.trim(), enableWebSearch);

        log.info("意图识别结果: {}", activation);
        // 构建运行时上下文，sessionId 必须与 Redis 键一致
        RuntimeContext runtimeContext = RuntimeContext.builder().userId(resolvedUserId).sessionId(sessionId).build();

        // 旧会话可能残留 DEFAULT 权限模式，改为 BYPASS 避免 MCP 工具 HITL 卡住
        harnessAgent.setPermissionMode(runtimeContext, PermissionMode.BYPASS);

        // 若有 ASKING 状态工具，自动附带 ConfirmResult 恢复执行
        UserMessage userMessage = buildUserMessage(resolvedUserId, sessionId, message.trim());
        // 用户消息立即写入 display_history，保证 UI 历史不丢
        appendDisplayMessage(resolvedUserId, sessionId, "user", message.trim());

        // 为本轮流式创建缓冲，用于停止时补存 assistant 片段
        String bufferKey = toStreamKey(resolvedUserId, sessionId);
        StreamSessionBuffer buffer = new StreamSessionBuffer();
        activeStreamBuffers.put(bufferKey, buffer);

        // 先推送 INTENT 事件，再推 Agent 流式事件
        Map<String, Object> intentPayload = new LinkedHashMap<>();
        intentPayload.put("type", "INTENT");
        intentPayload.put("intents", activation.intents());
        intentPayload.put("activeGroups", activation.activeGroups());
        intentPayload.put("enableWebSearch", activation.enableWebSearch());
        ServerSentEvent<String> intentEvent =
                ServerSentEvent.<String>builder()
                        .event("INTENT")
                        .data(toJson(intentPayload))
                        .build();

        // 走 HarnessAgent.streamEvents，事件与 Redis 持久化使用同一 sessionId
        return Flux.concat(
                        Flux.just(intentEvent),
                        harnessAgent
                                .streamEvents(userMessage, runtimeContext)
                                .doOnNext(
                                        event -> {
                                            // 累积文本增量，供停止/结束时写入
                                            if (event instanceof TextBlockDeltaEvent delta
                                                    && delta.getDelta() != null) {
                                                buffer.append(delta.getDelta());
                                            }
                                            // 正常结束标记，避免重复补写 assistant
                                            if (event.getType() == AgentEventType.AGENT_END) {
                                                buffer.markCompleted();
                                            }
                                        })
                                .map(this::toServerSentEvent))
                .doOnError(error ->
                                log.error("流式对话失败 userId={} sessionId={}", resolvedUserId, sessionId, error))
                .onErrorResume(error ->
                                // 向前端推送 ERROR 事件，避免 SSE 直接断连无提示
                                Flux.just(
                                        ServerSentEvent.<String>builder()
                                                .event("ERROR").data(toJson(Map.of("message", error.getMessage())))
                                                .build()))
                .doFinally(signal -> {
                            try {
                                // 补写 LLM context（agent_state）
                                flushPartialAssistantReply(resolvedUserId, sessionId, buffer);
                                // 补写 UI 展示历史（display_history）
                                appendDisplayAssistantFromBuffer(resolvedUserId, sessionId, buffer);
                                // 去掉框架插入的中断占位句
                                stripInterruptRecoveryMessage(resolvedUserId, sessionId);
                                // 持久化 agent_state 到 Redis
                                persistAgentState(resolvedUserId, sessionId, signal.name());
                            } finally {
                                activeStreamBuffers.remove(bufferKey); // 防止内存泄漏
                            }
                        });
    }

    /**
     * 用户点击「停止生成」：中断 Agent 并立即保存已生成内容。
     */
    public void stopChat(String userId, String sessionId) {
        String resolvedUserId = resolveUserId(userId);
        requireSessionId(sessionId);
        ReActAgent delegate = harnessAgent.getDelegate(); // 拿底层 Agent 调 interrupt
        delegate.interrupt(resolvedUserId, sessionId); // 触发中断信号

        String bufferKey = toStreamKey(resolvedUserId, sessionId);
        StreamSessionBuffer buffer = activeStreamBuffers.get(bufferKey);
        if (buffer != null && !buffer.isCompleted()) {
            // SSE 可能还在，主动 flush 已累积文本
            flushPartialAssistantReply(resolvedUserId, sessionId, buffer);
            appendDisplayAssistantFromBuffer(resolvedUserId, sessionId, buffer);
            stripInterruptRecoveryMessage(resolvedUserId, sessionId);
            persistAgentState(resolvedUserId, sessionId, "STOP");
        }
        log.info("已中断对话 userId={} sessionId={}", resolvedUserId, sessionId);
    }

    /** 删除会话：先 interrupt 再删 Redis 中该 session 全部 key */
    public void deleteSession(String userId, String sessionId) {
        String resolvedUserId = resolveUserId(userId);
        requireSessionId(sessionId);

        try {
            harnessAgent.getDelegate().interrupt(resolvedUserId, sessionId);
        } catch (Exception ignored) {
            // 无进行中的 call 时 interrupt 可能无效，忽略即可
        }
        agentStateStore.delete(resolvedUserId, sessionId); // 删除 agent_state + display_history 等
        log.info("已删除会话 userId={} sessionId={}", resolvedUserId, sessionId);
    }

    /** 将 AgentScope 事件转为前端 SSE 格式（含思考 / 工具过程） */
    private ServerSentEvent<String> toServerSentEvent(AgentEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", event.getType().name());
        payload.put("id", event.getId());

        if (event instanceof ThinkingBlockStartEvent thinkingStart) {
            payload.put("blockId", thinkingStart.getBlockId());
        } else if (event instanceof ThinkingBlockDeltaEvent thinkingDelta) {
            payload.put("blockId", thinkingDelta.getBlockId());
            payload.put("delta", thinkingDelta.getDelta());
        } else if (event instanceof ThinkingBlockEndEvent thinkingEnd) {
            payload.put("blockId", thinkingEnd.getBlockId());
        } else if (event instanceof TextBlockDeltaEvent delta) {
            payload.put("delta", delta.getDelta());
        } else if (event instanceof ToolCallStartEvent toolCall) {
            payload.put("toolCallId", toolCall.getToolCallId());
            putToolName(payload, toolCall.getToolCallName());
            log.info("工具调用开始 tool={} id={}", toolCall.getToolCallName(), toolCall.getToolCallId());
        } else if (event instanceof ToolCallDeltaEvent toolDelta) {
            payload.put("toolCallId", toolDelta.getToolCallId());
            // 流式分片时框架可能把 name 标成 __fragment__，前端用 id 合并，这里不透出脏名
            putToolName(payload, toolDelta.getToolCallName());
            payload.put("delta", toolDelta.getDelta());
        } else if (event instanceof ToolCallEndEvent toolEnd) {
            payload.put("toolCallId", toolEnd.getToolCallId());
            putToolName(payload, toolEnd.getToolCallName());
        } else if (event instanceof ToolResultStartEvent resultStart) {
            payload.put("toolCallId", resultStart.getToolCallId());
            putToolName(payload, resultStart.getToolCallName());
        } else if (event instanceof ToolResultTextDeltaEvent resultDelta) {
            payload.put("toolCallId", resultDelta.getToolCallId());
            putToolName(payload, resultDelta.getToolCallName());
            // 结果正文可能很大，前端时间线只用状态文案；仍透传 delta 供调试，但截断避免撑爆 SSE
            String delta = resultDelta.getDelta();
            if (delta != null && delta.length() > 4000) {
                payload.put("delta", delta.substring(0, 4000) + "…(truncated)");
                payload.put("truncated", true);
            } else {
                payload.put("delta", delta);
            }
        } else if (event instanceof ToolResultEndEvent resultEnd) {
            payload.put("toolCallId", resultEnd.getToolCallId());
            putToolName(payload, resultEnd.getToolCallName());
            if (resultEnd.getState() != null) {
                payload.put("state", resultEnd.getState().name());
            }
            log.info(
                    "工具调用结束 tool={} id={} state={}",
                    resultEnd.getToolCallName(),
                    resultEnd.getToolCallId(),
                    resultEnd.getState());
        } else if (event.getType() == AgentEventType.AGENT_END) {
            payload.put("done", true);
        }

        return ServerSentEvent.<String>builder()
                .event(event.getType().name())
                .data(toJson(payload))
                .build();
    }

    /** 过滤框架内部占位名（如 __fragment__），有效时才写入 payload */
    private static void putToolName(Map<String, Object> payload, String toolName) {
        String cleaned = sanitizeToolName(toolName);
        if (cleaned != null) {
            payload.put("toolName", cleaned);
        }
    }

    private static String sanitizeToolName(String toolName) {
        if (toolName == null || toolName.isBlank() || toolName.startsWith("__")) {
            return null;
        }
        return toolName;
    }

    /** 从 AgentState 转 ChatMessageVo，默认跳过 compaction 摘要 */
    private List<ChatMessageVo> toChatMessages(AgentState state) {
        return toChatMessages(state, true);
    }

    /**
     * 将 AgentState.context 转为展示消息列表。
     *
     * @param skipCompactionSummary 为 true 时跳过 __compaction_summary__ 摘要
     */
    private List<ChatMessageVo> toChatMessages(AgentState state, boolean skipCompactionSummary) {
        if (state == null || state.getContext() == null) {
            return List.of();
        }

        List<ChatMessageVo> messages = new ArrayList<>();
        for (Msg msg : state.getContext()) {
            MsgRole role = msg.getRole();
            if (role != MsgRole.USER && role != MsgRole.ASSISTANT) {
                continue; // 跳过 TOOL / SYSTEM 等
            }
            if (skipCompactionSummary && isCompactionSummaryMessage(msg)) {
                continue; // 不把 LLM 摘要展示给用户
            }
            if (isInterruptRecoveryMessage(msg)) {
                continue; // 跳过中断占位回复
            }
            String content = msg.getTextContent();
            if (role == MsgRole.ASSISTANT) {
                content = cleanAssistantContent(content); // 去掉思考块等
            }
            if (content == null || content.isBlank()) {
                continue;
            }
            messages.add(
                    ChatMessageVo.builder()
                            .role(role.name().toLowerCase())
                            .content(content.trim())
                            .build());
        }
        return messages;
    }

    /** 判断 AgentState 是否包含 compaction 摘要消息 */
    private boolean hasCompactionSummary(AgentState state) {
        if (state.getContext() == null) {
            return false;
        }
        return state.getContext().stream().anyMatch(this::isCompactionSummaryMessage);
    }

    /** 通过消息 name 识别 compaction 摘要 */
    private boolean isCompactionSummaryMessage(Msg msg) {
        return msg != null && COMPACTION_SUMMARY_NAME.equals(msg.getName());
    }

    /** 拼接 workspace 下完整历史 JSONL 路径（*.log.jsonl） */
    private Path resolveSessionLogFile(String userId, String sessionId) {
        return Path.of(properties.getWorkspace())
                .resolve("user-" + userId)
                .resolve("agents")
                .resolve(properties.getAgentName())
                .resolve("sessions")
                .resolve(sessionId + ".log.jsonl");
    }

    /** 从 JSONL 加载消息，默认清洗 assistant 内容 */
    private List<ChatMessageVo> loadMessagesFromSessionLog(Path logFile) {
        return loadMessagesFromSessionLog(logFile, false);
    }

    /**
     * 逐行解析 JSONL 归档。
     *
     * @param raw true 时保留 assistant 原文（含思考块）
     */
    private List<ChatMessageVo> loadMessagesFromSessionLog(Path logFile, boolean raw) {
        if (!Files.isRegularFile(logFile)) {
            return List.of(); // 归档不存在
        }
        List<ChatMessageVo> messages = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty()) {
                    continue;
                }
                JsonNode node = objectMapper.readTree(line); // 每行一个 JSON 对象
                if (!"message".equals(node.path("type").asText())) {
                    continue; // 只处理 message 类型条目
                }
                String role = node.path("role").asText("").toLowerCase();
                if (!"user".equals(role) && !"assistant".equals(role)) {
                    continue; // UI 不展示 TOOL 消息
                }
                String content = node.path("content").asText("");
                if (!raw && "assistant".equals(role)) {
                    content = cleanAssistantContent(content);
                }
                if (content.isBlank()) {
                    continue;
                }
                messages.add(
                        ChatMessageVo.builder().role(role).content(content.trim()).build());
            }
        } catch (Exception e) {
            log.warn("读取会话归档失败 path={}", logFile, e);
            return List.of();
        }
        return messages;
    }

    /** 从 Redis 读取 display_history */
    private List<ChatMessageVo> loadDisplayMessages(String userId, String sessionId) {
        return agentStateStore
                .get(userId, sessionId, DISPLAY_HISTORY_KEY, DisplayChatHistory.class)
                .map(h -> new ArrayList<>(h.getMessages())) // 拷贝一份避免修改缓存
                .orElseGet(ArrayList::new);
    }

    /** 将展示历史整表写回 Redis */
    private void saveDisplayMessages(String userId, String sessionId, List<ChatMessageVo> messages) {
        agentStateStore.save(
                userId, sessionId, DISPLAY_HISTORY_KEY, new DisplayChatHistory(messages));
    }

    /** 追加一条展示消息，相邻重复则跳过 */
    private void appendDisplayMessage(String userId, String sessionId, String role, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        List<ChatMessageVo> messages = loadDisplayMessages(userId, sessionId);
        ChatMessageVo last = messages.isEmpty() ? null : messages.get(messages.size() - 1);
        if (last != null
                && role.equals(last.getRole())
                && content.trim().equals(last.getContent())) {
            return; // 防重复写入
        }
        messages.add(ChatMessageVo.builder().role(role).content(content.trim()).build());
        saveDisplayMessages(userId, sessionId, messages);
    }

    /** 将本轮 SSE 缓冲的 assistant 文本写入 display_history（与页面一致） */
    private void appendDisplayAssistantFromBuffer(
            String userId, String sessionId, StreamSessionBuffer buffer) {
        if (buffer == null) {
            return;
        }
        String text = buffer.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        List<ChatMessageVo> messages = loadDisplayMessages(userId, sessionId);
        if (!messages.isEmpty()) {
            ChatMessageVo last = messages.get(messages.size() - 1);
            if ("assistant".equals(last.getRole()) && text.equals(last.getContent())) {
                return; // 已写入则跳过
            }
        }
        appendDisplayMessage(userId, sessionId, "assistant", text);
    }

    /** 旧会话首次打开：从 JSONL 回填 display_history（只执行一次） */
    private List<ChatMessageVo> backfillDisplayHistoryFromLog(
            String userId, String sessionId, Path archiveFile) {
        List<ChatMessageVo> collapsed = collapseLogToDisplayTurns(archiveFile);
        if (collapsed.isEmpty()) {
            return List.of();
        }
        saveDisplayMessages(userId, sessionId, collapsed); // 写入 Redis，后续不再回填
        log.info(
                "已从 JSONL 回填展示历史 userId={} sessionId={} messages={}",
                userId,
                sessionId,
                collapsed.size());
        return collapsed;
    }

    /**
     * 将 JSONL 中多轮工具/思考 assistant 合并为「一问一答」，保留用户原文。
     * 原因：ReAct 每调一次工具会产生一条 assistant，直接展示会很乱。
     */
    private List<ChatMessageVo> collapseLogToDisplayTurns(Path logFile) {
        if (!Files.isRegularFile(logFile)) {
            return List.of();
        }
        List<ChatMessageVo> result = new ArrayList<>();
        String pendingAssistant = null; // 暂存当前用户轮次内最后一条有效 assistant
        try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty()) {
                    continue;
                }
                JsonNode node = objectMapper.readTree(line);
                if (!"message".equals(node.path("type").asText())) {
                    continue;
                }
                String role = node.path("role").asText("").toLowerCase();
                String content = node.path("content").asText("").trim();
                if ("user".equals(role)) {
                    // 遇到新用户消息前，先把上一轮 assistant 落盘
                    if (pendingAssistant != null) {
                        result.add(
                                ChatMessageVo.builder()
                                        .role("assistant")
                                        .content(pendingAssistant)
                                        .build());
                        pendingAssistant = null;
                    }
                    if (!content.isBlank()) {
                        result.add(ChatMessageVo.builder().role("user").content(content).build());
                    }
                } else if ("assistant".equals(role)) {
                    String cleaned = cleanAssistantContent(content);
                    if (!cleaned.isBlank()) {
                        pendingAssistant = cleaned; // 覆盖为同轮最后一条有效回复
                    }
                }
            }
        } catch (Exception e) {
            log.warn("回填展示历史失败 path={}", logFile, e);
            return List.of();
        }
        // 文件末尾若还有未落盘的 assistant
        if (pendingAssistant != null) {
            result.add(
                    ChatMessageVo.builder().role("assistant").content(pendingAssistant).build());
        }
        return result;
    }

    /** 清洗 assistant：去掉 Qwen 思考块、纯工具调用行 */
    private String cleanAssistantContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String text = content.strip();
        // Qwen 思考块结束标签（流式/部分模型输出）
        text = stripAfterTag(text, "");
        text = stripAfterTag(text, "</think>"); // 归档 JSONL 里的思考标签
        if (text.startsWith("[tool_call:") && !text.contains("\n")) {
            return ""; // 仅工具调用、无用户可见正文则跳过
        }
        return text;
    }

    /** 取 tag 之后的内容（思考块在用户可见正文之前） */
    private static String stripAfterTag(String text, String tag) {
        int idx = text.lastIndexOf(tag);
        if (idx >= 0 && idx + tag.length() < text.length()) {
            return text.substring(idx + tag.length()).strip();
        }
        return text;
    }

    /** 校验并规范化 history source 参数 */
    private String normalizeHistorySource(String source) {
        if (source == null || source.isBlank()) {
            return "auto";
        }
        String mode = source.trim().toLowerCase();
        if ("context".equals(mode) || "full".equals(mode) || "display".equals(mode) || "auto".equals(mode)) {
            return mode;
        }
        throw new IllegalArgumentException("source 仅支持 auto、display、context、full");
    }

    /** 会话列表标题：优先 display_history 首条用户消息 */
    private String deriveSessionTitle(String userId, String sessionId) {
        return loadDisplayMessages(userId, sessionId).stream()
                .filter(msg -> "user".equals(msg.getRole()))
                .map(ChatMessageVo::getContent)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .map(text -> text.length() > 24 ? text.substring(0, 24) + "…" : text)
                .orElseGet(
                        () ->
                                agentStateStore
                                        .get(userId, sessionId, AGENT_STATE_KEY, AgentState.class)
                                        .map(this::firstUserMessageTitle)
                                        .orElse("新对话"));
    }

    /** 从 agent_state 取首条用户消息作标题（display 为空时的兜底） */
    private String firstUserMessageTitle(AgentState state) {
        if (state.getContext() == null) {
            return "新对话";
        }
        return state.getContext().stream()
                .filter(msg -> msg.getRole() == MsgRole.USER)
                .map(Msg::getTextContent)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .map(text -> text.length() > 24 ? text.substring(0, 24) + "…" : text)
                .orElse("新对话");
    }

    /**
     * 构造发给 Agent 的 UserMessage。
     * 若上次 HITL 有 ASKING 工具未确认，自动附带 ConfirmResult(true) 恢复。
     */
    private UserMessage buildUserMessage(String userId, String sessionId, String text) {
        List<ConfirmResult> pendingConfirms = collectPendingConfirmResults(userId, sessionId);
        if (pendingConfirms.isEmpty()) {
            return new UserMessage(text); // 普通用户消息
        }
        log.info(
                "自动确认 {} 个待审批工具调用 userId={} sessionId={}",
                pendingConfirms.size(),
                userId,
                sessionId);
        return UserMessage.builder()
                .textContent(text)
                .metadata(Map.of(Msg.METADATA_CONFIRM_RESULTS, pendingConfirms)) // AgentScope 约定 key
                .build();
    }

    /** 显式调用 saveAgentState 写入 Redis */
    private void persistAgentState(String userId, String sessionId, String signal) {
        try {
            harnessAgent.getDelegate().saveAgentState(userId, sessionId);
            log.info("会话状态已持久化 userId={} sessionId={} signal={}", userId, sessionId, signal);
        } catch (Exception e) {
            log.warn("会话状态持久化失败 userId={} sessionId={}", userId, sessionId, e);
        }
    }

    /**
     * 将 SSE 已推送文本补写入 agent_state.context。
     * 原因：Agent 仅在 reasoning 正常结束或 InterruptedException 时 buildFinalMessage，
     * 用户点停止时常来不及写入。
     */
    private void flushPartialAssistantReply(
            String userId, String sessionId, StreamSessionBuffer buffer) {
        if (buffer == null || buffer.isCompleted()) {
            return; // 已正常结束则 framework 已写入
        }
        String partial = buffer.getText().trim();
        if (partial.isEmpty()) {
            return;
        }

        AgentState state = harnessAgent.getDelegate().getAgentState(userId, sessionId);
        if (state == null || state.getContext() == null) {
            return;
        }

        List<Msg> context = state.contextMutable(); // 可写 context 列表
        if (context.isEmpty()) {
            return;
        }

        Msg last = context.get(context.size() - 1);
        if (last.getRole() == MsgRole.ASSISTANT) {
            if (isInterruptRecoveryMessage(last)) {
                // 用真实片段替换中断占位
                context.set(context.size() - 1, new AssistantMessage(partial));
                log.info(
                        "已用部分回复替换中断占位 userId={} sessionId={} chars={}",
                        userId,
                        sessionId,
                        partial.length());
                return;
            }
            String existing = last.getTextContent();
            if (existing != null && existing.trim().length() >= partial.length()) {
                return; // 已有更长内容则不覆盖
            }
            context.set(context.size() - 1, new AssistantMessage(partial));
            log.info("已更新部分 assistant 回复 userId={} sessionId={} chars={}", userId, sessionId, partial.length());
            return;
        }

        if (last.getRole() == MsgRole.USER) {
            // 最后一条是用户消息，追加 assistant 部分回复
            context.add(new AssistantMessage(partial));
            log.info(
                    "已保存中断时的部分 assistant 回复 userId={} sessionId={} chars={}",
                    userId,
                    sessionId,
                    partial.length());
        }
    }

    /** 流式缓冲 Map 的 key：userId + sessionId */
    private static String toStreamKey(String userId, String sessionId) {
        return userId + "/" + sessionId;
    }

    /** 单会话流式文本缓冲（线程内使用，ConcurrentHashMap 保证多会话并发） */
    private static final class StreamSessionBuffer {
        /** 累积 SSE 文本 delta */
        private final StringBuilder text = new StringBuilder();
        /** 是否已正常结束（AGENT_END） */
        private volatile boolean completed;

        void append(String delta) {
            text.append(delta);
        }

        String getText() {
            return text.toString();
        }

        void markCompleted() {
            completed = true;
        }

        boolean isCompleted() {
            return completed;
        }
    }

    /** 从 agent_state 移除框架插入的中断占位 assistant 消息 */
    private void stripInterruptRecoveryMessage(String userId, String sessionId) {
        AgentState state = harnessAgent.getDelegate().getAgentState(userId, sessionId);
        if (state == null || state.getContext() == null || state.getContext().isEmpty()) {
            return;
        }
        List<Msg> context = state.contextMutable();
        for (int i = context.size() - 1; i >= 0; i--) {
            if (isInterruptRecoveryMessage(context.get(i))) {
                context.remove(i);
                break; // 通常只有一条
            }
        }
    }

    /** 判断是否为 interrupt 占位回复 */
    private boolean isInterruptRecoveryMessage(Msg msg) {
        if (msg == null || msg.getRole() != MsgRole.ASSISTANT) {
            return false;
        }
        String text = msg.getTextContent();
        return text != null && text.contains(INTERRUPT_RECOVERY_SNIPPET);
    }

    /** 扫描 context 中 ASKING 状态工具，生成自动 ConfirmResult */
    private List<ConfirmResult> collectPendingConfirmResults(String userId, String sessionId) {
        Optional<AgentState> stateOpt =
                agentStateStore.get(userId, sessionId, AGENT_STATE_KEY, AgentState.class);
        if (stateOpt.isEmpty() || stateOpt.get().getContext() == null) {
            return List.of();
        }

        List<ConfirmResult> results = new ArrayList<>();
        for (Msg msg : stateOpt.get().getContext()) {
            if (msg.getRole() != MsgRole.ASSISTANT) {
                continue;
            }
            for (ToolUseBlock block : msg.getContentBlocks(ToolUseBlock.class)) {
                if (block.getState() == ToolCallState.ASKING) {
                    results.add(new ConfirmResult(true, block)); // true = 用户确认执行
                }
            }
        }
        return results;
    }

    /** 校验 userId 非空并 trim */
    private String resolveUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        return userId.trim();
    }

    /** 校验 sessionId 非空 */
    private void requireSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
    }

    /** 校验 message 非空 */
    private void requireMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }

    /** 对象转 JSON 字符串，失败时返回固定错误 JSON */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"message\":\"serialize error\"}";
        }
    }
}
