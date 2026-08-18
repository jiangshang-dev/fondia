package org.jeecg.modules.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.*;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.exception.BadRequestException;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.IdGeneratorUtil;
import org.jeecg.modules.ai.api.IAiUserResolver;
import org.jeecg.modules.ai.config.AgentScopeProperties;
import org.jeecg.modules.ai.dto.AiChatDTO;
import org.jeecg.modules.ai.dto.ChatDtos;
import org.jeecg.modules.ai.dto.resp.SseResponse;
import org.jeecg.modules.ai.service.ChatService;
import org.jeecg.modules.ai.service.ToolThinkingService;
import org.jeecg.modules.ai.vo.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

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

    @Override
    public List<AiSessionSummaryVO> historyList(String authorization, String source, Integer pageNo, Integer pageSize) {
        return List.of();
    }

    @Override
    public AiSessionVO createSession(String authorization, String source) {
        return null;
    }

    @Override
    public List<AiMessageVO> messagesById(String authorization, String id) {
        return List.of();
    }

    @Override
    public void stopChat(String authorization, ChatDtos.StopChatRequest request) {

    }

    @Override
    public Flux<SseResponse<MessageDataVO<Object>>> chat(String authorization, AiChatDTO dto) {
        String userId = resolveUserId(authorization);
        Msg userMsg = new UserMessage(dto.getContent());
        RuntimeContext runtimeContext = RuntimeContext.builder().userId(userId).sessionId(dto.getSessionId()).build();

        Map<String, StringBuilder> toolResultBuffers = new LinkedHashMap<>();
        Map<String, ThinkingStepVO> toolStepIndex = new ConcurrentHashMap<>();
        List<ThinkingStepVO> thinkingSteps = Collections.synchronizedList(new ArrayList<>());

        Flux<SseResponse<MessageDataVO<Object>>> sseResponseFlux = harnessAgent.streamEvents(userMsg, runtimeContext)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnCancel(() -> {
                    log.warn("前端连接主动断开或取消会话聊天，会话ID: {}", dto.getSessionId());
                })
                .concatMap(agentEvent -> {
                    if (agentEvent instanceof ThinkingBlockDeltaEvent thinkingBlockDeltaEvent) {
                        String delta = thinkingBlockDeltaEvent.getDelta();

                        MessageDataVO<Object> messageData = new MessageDataVO<>();
                        messageData.setMessage(delta);
                        messageData.setStreamEvent(THINKING_DELTA);
                        messageData.setTimestamp(System.currentTimeMillis());
                        return Flux.just(SseResponse.chunkThinking(dto.getSessionId(), messageData));
                    }

                    if (agentEvent instanceof TextBlockDeltaEvent textBlockDeltaEvent) {
                        String delta = textBlockDeltaEvent.getDelta();
                        List<SseResponse<MessageDataVO<Object>>> out = new ArrayList<>();
                        MessageDataVO<Object> body = new MessageDataVO<>();
                        body.setStreamEvent(ANSWER_DELTA);
                        body.setMessage(delta);
                        body.setTimestamp(System.currentTimeMillis());
                        out.add(SseResponse.chunk(dto.getSessionId(), body));
                        return Flux.fromIterable(out);
                    }

                    if (agentEvent instanceof RequireUserConfirmEvent requireUserConfirmEvent) {
                        return Flux.error(new JeecgBootException("工具权限确认异常，已中止本轮"));
                    }

                    if (agentEvent instanceof ToolCallStartEvent toolCallStartEvent) {
                        MessageDataVO<Object> toolData = toolThinkingService.buildToolStreamData(TOOL_CALL_START, toolCallStartEvent.getToolCallId(), toolCallStartEvent.getToolCallName(), null);
                        toolData.setStreamEvent(TOOL_CALL_START);
                        return Flux.just(SseResponse.chunkTool(dto.getSessionId(), toolData));
                    }

                    if (agentEvent instanceof ToolCallDeltaEvent toolCallDeltaEvent) {
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolCallDeltaEvent.getToolCallId(),
                                toolCallDeltaEvent.getToolCallName(),
                                toolCallDeltaEvent.getDelta(),
                                "正在调用 " + toolCallDeltaEvent.getToolCallName(),
                                RUNNING);
                        MessageDataVO<Object> toolStreamData = toolThinkingService.buildToolStreamData(TOOL_CALL_DELTA, toolCallDeltaEvent.getToolCallId(), toolCallDeltaEvent.getToolCallName(), toolCallDeltaEvent.getDelta());
                        return Flux.just(SseResponse.chunkTool(dto.getSessionId(), toolStreamData));
                    }

                    if (agentEvent instanceof ToolCallEndEvent toolCallEndEvent) {
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolCallEndEvent.getToolCallId(), toolCallEndEvent.getToolCallName(), null, "已准备调用 " + toolCallEndEvent.getToolCallName(), RUNNING);
                        return Flux.just(SseResponse.chunkTool(dto.getSessionId(), toolThinkingService.buildToolStreamData(TOOL_CALL_END, toolCallEndEvent.getToolCallId(), toolCallEndEvent.getToolCallName(), null)));
                    }

                    if (agentEvent instanceof ToolResultStartEvent toolResultStartEvent) {
                        toolThinkingService.upsertToolStep(thinkingSteps, toolStepIndex, toolResultStartEvent.getToolCallId(), toolResultStartEvent.getToolCallName(), null, "正在执行 " + toolResultStartEvent.getToolCallName(), RUNNING);
                        return Flux.just(SseResponse.chunkTool(dto.getSessionId(), toolThinkingService.buildToolStreamData(TOOL_RESULT_START, toolResultStartEvent.getToolCallId(), toolResultStartEvent.getToolCallName(), null)));
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
                        return Flux.just(SseResponse.chunkTool(dto.getSessionId(), toolThinkingService.buildToolStreamData(TOOL_RESULT_END, toolResultEndEvent.getToolCallId(), toolResultEndEvent.getToolCallName(), state)));
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
                    return Flux.just(SseResponse.chunk(dto.getSessionId(), errData), SseResponse.endFlux(userMsgId, assistantMsgId));
                })
                .doFinally(signalType -> {
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
}
