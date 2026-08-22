package org.jeecg.modules.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.*;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.exception.BadRequestException;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.IdGeneratorUtil;
import org.jeecg.modules.ai.api.IAiUserResolver;
import org.jeecg.modules.ai.dto.AiChatDTO;
import org.jeecg.modules.ai.dto.ChatDtos;
import org.jeecg.modules.ai.dto.resp.SseResponse;
import org.jeecg.modules.ai.entity.FdAiMessage;
import org.jeecg.modules.ai.entity.FdAiSession;
import org.jeecg.modules.ai.mapper.FdAiMessageMapper;
import org.jeecg.modules.ai.mapper.FdAiSessionMapper;
import org.jeecg.modules.ai.service.ChatService;
import org.jeecg.modules.ai.service.ToolThinkingService;
import org.jeecg.modules.ai.vo.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jeecg.modules.ai.constant.ToolStatusConstant.DONE;
import static org.jeecg.modules.ai.constant.ToolStatusConstant.RUNNING;
import static org.jeecg.modules.ai.constant.ToolThinkConstant.*;

@Slf4j
@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final IAiUserResolver userResolver;
    private final ToolThinkingService toolThinkingService;
    private final HarnessAgent harnessAgent;
    private final FdAiSessionMapper sessionMapper;
    private final FdAiMessageMapper messageMapper;
    private final Map<String, AtomicBoolean> stopSignals = new ConcurrentHashMap<>();

    @Override
    public List<AiSessionSummaryVO> historyList(String authorization, String source, Integer pageNo, Integer pageSize) {
        Long userId = requireUserId(authorization);
        int page = Math.max(pageNo == null ? 1 : pageNo, 1);
        int size = Math.min(Math.max(pageSize == null ? 20 : pageSize, 1), 100);
        List<FdAiSession> sessions = sessionMapper.selectList(new LambdaQueryWrapper<FdAiSession>()
                .eq(FdAiSession::getUserId, userId)
                .eq(FdAiSession::getSource, normalizeSource(source))
                .orderByDesc(FdAiSession::getLastMessageTime)
                .last("LIMIT " + ((page - 1) * size) + "," + size));
        return sessions.stream().map(this::toSummary).toList();
    }

    @Override
    public AiSessionVO createSession(String authorization, String source) {
        Long userId = requireUserId(authorization);
        return toSession(createSession(userId, normalizeSource(source)));
    }

    @Override
    public AiSessionVO activeSession(String authorization, String source) {
        Long userId = requireUserId(authorization);
        FdAiSession session = sessionMapper.selectOne(new LambdaQueryWrapper<FdAiSession>()
                .eq(FdAiSession::getUserId, userId)
                .eq(FdAiSession::getSource, normalizeSource(source))
                .orderByDesc(FdAiSession::getLastMessageTime)
                .last("LIMIT 1"));
        return session == null ? null : toSession(session);
    }

    @Override
    public List<AiMessageVO> messagesById(String authorization, String id) {
        Long userId = requireUserId(authorization);
        FdAiSession session = requireSession(userId, id);
        return messageMapper.selectList(new LambdaQueryWrapper<FdAiMessage>()
                        .eq(FdAiMessage::getSessionId, session.getId())
                        .orderByAsc(FdAiMessage::getCreateTime))
                .stream().map(this::toMessage).toList();
    }

    @Override
    public void stopChat(String authorization, ChatDtos.StopChatRequest request) {
        Long userId = requireUserId(authorization);
        if (request == null || request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new JeecgBootException("会话ID不能为空");
        }
        FdAiSession session = requireSession(userId, request.getSessionId());
        stopSignals.computeIfAbsent(session.getId().toString(), key -> new AtomicBoolean()).set(true);
        long now = System.currentTimeMillis();
        session.setStatus("STOPPED").setUpdateTime(now);
        sessionMapper.updateById(session);
    }

    @Override
    public Flux<SseResponse<MessageDataVO<Object>>> chat(String authorization, AiChatDTO dto) {
        Long userId = requireUserId(authorization);
        FdAiSession session = resolveChatSession(userId, dto);
        String sessionId = session.getId().toString();
        AtomicBoolean stopSignal = new AtomicBoolean(false);
        stopSignals.put(sessionId, stopSignal);
        saveUserMessage(session, dto.getContent());
        session.setStatus("PROCESSING").setLastMessage(dto.getContent()).setLastMessageTime(System.currentTimeMillis()).setUpdateTime(System.currentTimeMillis());
        sessionMapper.updateById(session);
        Msg userMsg = new UserMessage(dto.getContent());
        RuntimeContext runtimeContext = RuntimeContext.builder().userId(String.valueOf(userId)).sessionId(sessionId).build();
        StringBuilder answerBuffer = new StringBuilder();

        Map<String, StringBuilder> toolResultBuffers = new LinkedHashMap<>();
        Map<String, ThinkingStepVO> toolStepIndex = new ConcurrentHashMap<>();
        List<ThinkingStepVO> thinkingSteps = Collections.synchronizedList(new ArrayList<>());

        Flux<SseResponse<MessageDataVO<Object>>> sseResponseFlux = harnessAgent.streamEvents(userMsg, runtimeContext)
                .subscribeOn(Schedulers.boundedElastic())
                .takeWhile(event -> !stopSignal.get())
                .doOnCancel(() -> {
                    log.warn("前端连接主动断开或取消会话聊天，会话ID: {}", dto.getSessionId());
                })
                .concatMap(agentEvent -> {
                    String source = agentEvent.getSource();
                    boolean fromSubagent = source != null && !source.isBlank();

                    if (fromSubagent && agentEvent instanceof AgentStartEvent) {
                        MessageDataVO<Object> messageData = new MessageDataVO<>();
                        messageData.setMessage(subagentDisplayName(source) + "开始处理");
                        messageData.setStreamEvent(THINKING_DELTA);
                        messageData.setTimestamp(System.currentTimeMillis());
                        log.info("子 Agent 启动 sessionId={} source={}", dto.getSessionId(), source);
                        return Flux.just(SseResponse.chunkThinking(sessionId, messageData));
                    }

                    if (agentEvent instanceof ThinkingBlockDeltaEvent thinkingBlockDeltaEvent) {
                        String delta = thinkingBlockDeltaEvent.getDelta();
                        MessageDataVO<Object> messageData = new MessageDataVO<>();
                        messageData.setMessage(fromSubagent ? "[" + subagentDisplayName(source) + "] " + delta : delta);
                        messageData.setStreamEvent(THINKING_DELTA);
                        messageData.setTimestamp(System.currentTimeMillis());
                        return Flux.just(SseResponse.chunkThinking(sessionId, messageData));
                    }

                    if (agentEvent instanceof TextBlockDeltaEvent textBlockDeltaEvent) {
                        String delta = textBlockDeltaEvent.getDelta();
                        MessageDataVO<Object> body = new MessageDataVO<>();
                        body.setMessage(delta);
                        body.setTimestamp(System.currentTimeMillis());
                        if (fromSubagent) {
                            // 子 Agent 文本不作为最终回答，避免和总管回复混在一起
                            body.setStreamEvent(THINKING_DELTA);
                            body.setMessage("[" + subagentDisplayName(source) + "] " + delta);
                            return Flux.just(SseResponse.chunkThinking(sessionId, body));
                        }
                        answerBuffer.append(delta);
                        body.setStreamEvent(ANSWER_DELTA);
                        return Flux.just(SseResponse.chunk(sessionId, body));
                    }

                    if (agentEvent instanceof RequireUserConfirmEvent requireUserConfirmEvent) {
                        return Flux.error(new JeecgBootException("工具权限确认异常，已中止本轮"));
                    }

                    if (agentEvent instanceof ToolCallStartEvent toolCallStartEvent) {
                        log.info("工具开始调用 sessionId={} tool={} source={}",
                                dto.getSessionId(), toolCallStartEvent.getToolCallName(), source);
                        MessageDataVO<Object> toolData = toolThinkingService.buildToolStreamData(TOOL_CALL_START, toolCallStartEvent.getToolCallId(), toolCallStartEvent.getToolCallName(), null);
                        toolData.setStreamEvent(TOOL_CALL_START);
                        return Flux.just(SseResponse.chunkTool(sessionId, toolData));
                    }

                    if (agentEvent instanceof ToolCallDeltaEvent toolCallDeltaEvent) {
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolCallDeltaEvent.getToolCallId(),
                                toolCallDeltaEvent.getToolCallName(),
                                toolCallDeltaEvent.getDelta(),
                                "正在调用 " + toolCallDeltaEvent.getToolCallName(),
                                RUNNING);
                        MessageDataVO<Object> toolStreamData = toolThinkingService.buildToolStreamData(TOOL_CALL_DELTA, toolCallDeltaEvent.getToolCallId(), toolCallDeltaEvent.getToolCallName(), toolCallDeltaEvent.getDelta());
                        return Flux.just(SseResponse.chunkTool(sessionId, toolStreamData));
                    }

                    if (agentEvent instanceof ToolCallEndEvent toolCallEndEvent) {
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolCallEndEvent.getToolCallId(), toolCallEndEvent.getToolCallName(), null, "已准备调用 " + toolCallEndEvent.getToolCallName(), RUNNING);
                        return Flux.just(SseResponse.chunkTool(sessionId, toolThinkingService.buildToolStreamData(TOOL_CALL_END, toolCallEndEvent.getToolCallId(), toolCallEndEvent.getToolCallName(), null)));
                    }

                    if (agentEvent instanceof ToolResultStartEvent toolResultStartEvent) {
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolResultStartEvent.getToolCallId(), toolResultStartEvent.getToolCallName(), null, "正在执行 " + toolResultStartEvent.getToolCallName(), RUNNING);
                        return Flux.just(SseResponse.chunkTool(sessionId, toolThinkingService.buildToolStreamData(TOOL_RESULT_START, toolResultStartEvent.getToolCallId(), toolResultStartEvent.getToolCallName(), null)));
                    }

                    if (agentEvent instanceof ToolResultTextDeltaEvent toolResultTextDeltaEvent) {
                        toolThinkingService.appendToolResultDelta(toolResultBuffers, toolResultTextDeltaEvent.getToolCallId(), toolResultTextDeltaEvent.getDelta());
                        return Flux.empty();
                    }

                    if (agentEvent instanceof ToolResultDataDeltaEvent toolResultDataDeltaEvent) {
                        String toolResultText = toolThinkingService.extractToolResultText(toolResultDataDeltaEvent.getData());
                        toolThinkingService.appendToolResultDelta(toolResultBuffers, toolResultDataDeltaEvent.getToolCallId(), toolResultText);
                        return Flux.empty();
                    }

                    if (agentEvent instanceof ToolResultEndEvent toolResultEndEvent) {
                        log.info("MCP工具 [{}] 执行完成, sessionId={}", toolResultEndEvent.getToolCallName(), dto.getSessionId());
                        String state = toolResultEndEvent.getState() == null ? null : toolResultEndEvent.getState().name();
                        String content = state != null && !"SUCCESS".equals(state)
                                ? toolResultEndEvent.getToolCallName() + " 执行结束（" + state + "）"
                                : toolResultEndEvent.getToolCallName() + " 执行完成";
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolResultEndEvent.getToolCallId(), toolResultEndEvent.getToolCallName(), null, content, DONE);
                        return Flux.just(SseResponse.chunkTool(sessionId, toolThinkingService.buildToolStreamData(TOOL_RESULT_END, toolResultEndEvent.getToolCallId(), toolResultEndEvent.getToolCallName(), state)));
                    }
                    return Flux.empty();
                })
                .onErrorResume(Throwable.class, (e) -> {
                    log.error("SSE 响应流发生异常, sessionId={}", dto.getSessionId(), e);
                    String errorMsg = "系统响应异常，请稍后重试";
                    if (e.getMessage() != null && e.getMessage().contains("maximum context length")) {
                        errorMsg = "对话上下文超出模型限制，请新建会话或清空历史记录。" + e.getMessage();
                    } else if (e instanceof BadRequestException) {
                        errorMsg = e.getMessage();
                    }
                    MessageDataVO<Object> errData = new MessageDataVO<>();
                    errData.setMessage(errorMsg);
                    errData.setStreamEvent("ERROR");
                    errData.setTimestamp(System.currentTimeMillis());

                    String userMsgId = IdGeneratorUtil.getSnowflakeNextIdStr();
                    String assistantMsgId = IdGeneratorUtil.getSnowflakeNextIdStr();
                    return Flux.just(SseResponse.chunk(sessionId, errData), SseResponse.endFlux(userMsgId, assistantMsgId));
                })
                .doFinally(signalType -> {
                    persistAssistantMessage(session, answerBuffer.toString(), stopSignal.get(), signalType.name());
                    stopSignals.remove(sessionId, stopSignal);
                    String toolLink = toolThinkingService.resolveToolResult(toolResultBuffers);
                    log.info("工具链接: {}", toolLink);
                })
                ;
        return sseResponseFlux;
    }

    private String resolveUserId(String authorization) {
        Long userId = userResolver.resolveUserId(authorization);
        if (userId == null) {
            throw new JeecgBootException("用户未登录或Token无效");
        }
        return String.valueOf(userId);
    }

    private Long requireUserId(String authorization) {
        return Long.valueOf(resolveUserId(authorization));
    }

    private FdAiSession resolveChatSession(Long userId, AiChatDTO dto) {
        if (dto.getSessionId() == null || dto.getSessionId().isBlank()) {
            return createSession(userId, normalizeSource(dto.getSource()));
        }
        return requireSession(userId, dto.getSessionId());
    }

    private FdAiSession requireSession(Long userId, String id) {
        Long sessionId;
        try {
            sessionId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new JeecgBootException("会话不存在");
        }
        FdAiSession session = sessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在或无权访问");
        }
        return session;
    }

    private FdAiSession createSession(Long userId, String source) {
        long now = System.currentTimeMillis();
        FdAiSession session = new FdAiSession().setUserId(userId).setSource(source).setStatus("ACTIVE")
                .setCreateTime(now).setUpdateTime(now).setLastMessageTime(now);
        sessionMapper.insert(session);
        return session;
    }

    private void saveUserMessage(FdAiSession session, String content) {
        long now = System.currentTimeMillis();
        messageMapper.insert(new FdAiMessage().setSessionId(session.getId()).setUserId(session.getUserId())
                .setSenderType("USER").setMsgType("TEXT").setContent(content).setStatus("COMPLETED")
                .setCreateTime(now).setUpdateTime(now));
        if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.setTitle(abbreviate(content, 200));
        }
    }

    private void persistAssistantMessage(FdAiSession session, String answer, boolean stopped, String signal) {
        long now = System.currentTimeMillis();
        String content = answer.isBlank() && stopped ? "已停止生成" : answer;
        if (!content.isBlank()) {
            messageMapper.insert(new FdAiMessage().setSessionId(session.getId()).setUserId(session.getUserId())
                    .setSenderType("AI").setMsgType("TEXT").setContent(content)
                    .setStatus(stopped ? "STOPPED" : "COMPLETED").setCreateTime(now).setUpdateTime(now));
        }
        session.setStatus(stopped ? "STOPPED" : "COMPLETED").setLastMessage(abbreviate(content, 500))
                .setLastMessageTime(now).setUpdateTime(now);
        sessionMapper.updateById(session);
    }

    private AiSessionVO toSession(FdAiSession session) {
        AiSessionVO vo = new AiSessionVO();
        vo.setId(session.getId().toString()); vo.setStatus(session.getStatus()); vo.setSource(session.getSource()); vo.setCreateTime(session.getCreateTime());
        return vo;
    }

    private AiSessionSummaryVO toSummary(FdAiSession session) {
        AiSessionSummaryVO vo = new AiSessionSummaryVO();
        vo.setId(session.getId().toString()); vo.setSource(session.getSource()); vo.setLastMessage(session.getLastMessage()); vo.setLastMessageTime(session.getLastMessageTime());
        return vo;
    }

    private AiMessageVO toMessage(FdAiMessage message) {
        AiMessageVO vo = new AiMessageVO();
        vo.setId(message.getId().toString()); vo.setSessionId(message.getSessionId().toString()); vo.setSenderType(message.getSenderType());
        vo.setMsgType(message.getMsgType()); vo.setContent(message.getContent()); vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private static String normalizeSource(String source) { return source == null || source.isBlank() ? "ai_tab" : source; }
    private static String abbreviate(String value, int max) { return value == null ? "" : value.length() <= max ? value : value.substring(0, max); }

    private static String subagentDisplayName(String source) {
        String id = source;
        int slash = source.lastIndexOf('/');
        if (slash >= 0 && slash < source.length() - 1) {
            id = source.substring(slash + 1);
        }
        return switch (id) {
            case "menu-agent" -> "菜谱专家";
            case "nutrition-agent" -> "营养专家";
            case "ingredient-agent" -> "食材专家";
            case "plan-agent" -> "饮食计划专家";
            case "cooking-agent" -> "烹饪专家";
            default -> id;
        };
    }
}
