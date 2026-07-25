package org.jeecg.modules.ai.service;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.ai.api.IAiUserResolver;
import org.jeecg.modules.ai.config.AiAsyncExecutorConfig;
import org.jeecg.modules.ai.dto.AiChatDTO;
import org.jeecg.modules.ai.dto.ChatDtos.StopChatRequest;
import org.jeecg.modules.ai.vo.AiSessionSummaryVO;
import org.jeecg.modules.ai.vo.AiMessageVO;
import org.jeecg.modules.ai.vo.AiSessionVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * fondia_app 契约适配：会话 CRUD + AgentScope 流式对话 → Airag/App SSE 事件。
 */
@Slf4j
@Service
public class SupportAiChatService {

    private final IAiUserResolver userResolver;
    private final SessionMetaService sessionMetaService;
    private final ChatService chatService;
    private final Executor aiChatExecutor;

    public SupportAiChatService(
            IAiUserResolver userResolver,
            SessionMetaService sessionMetaService,
            ChatService chatService,
            @Qualifier(AiAsyncExecutorConfig.AI_CHAT_EXECUTOR) Executor aiChatExecutor) {
        this.userResolver = userResolver;
        this.sessionMetaService = sessionMetaService;
        this.chatService = chatService;
        this.aiChatExecutor = aiChatExecutor;
    }

    public List<AiSessionSummaryVO> listSessions(String authorization, String source, Integer pageNo, Integer pageSize) {
        String userId = resolveUserId(authorization);
        return sessionMetaService.listSessions(
                userId,
                source,
                pageNo == null ? 1 : pageNo,
                pageSize == null ? 20 : pageSize);
    }

    public AiSessionVO createSession(String authorization, String source) {
        return sessionMetaService.createSession(resolveUserId(authorization), source);
    }

    public AiSessionVO getActiveSession(String authorization, String source) {
        return sessionMetaService.getActiveSession(resolveUserId(authorization), source);
    }

    public void stopChat(String authorization, StopChatRequest request) {
        String userId = resolveUserId(authorization);
        if (request == null || request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new JeecgBootException("sessionId 不能为空");
        }
        chatService.stopChat(userId, request.getSessionId());
    }

    /**
     * POST /agent/ai/chat — SSE，事件体与 Airag/fondia 一致：
     * MESSAGE / APP_META / APP_ERROR。
     */
    public SseEmitter chatStream(String authorization, AiChatDTO dto) {
        String userId = resolveUserId(authorization);
        AiSessionVO session = sessionMetaService.resolveForChat(userId, dto.getSessionId(), dto.getSource());
        String sessionId = session.getId();
        sessionMetaService.ensureMeta(userId, sessionId, dto.getSource());
        sessionMetaService.touchSession(userId, sessionId, dto.getContent());

        SseEmitter emitter = new SseEmitter(0L);
        final String message = dto.getContent();

        aiChatExecutor.execute(() -> runStream(emitter, userId, sessionId, message));
        return emitter;
    }

    private void runStream(SseEmitter emitter, String userId, String sessionId, String message) {
        AtomicReference<String> replyHolder = new AtomicReference<>("");
        java.util.concurrent.atomic.AtomicBoolean errorSent = new java.util.concurrent.atomic.AtomicBoolean(false);
        try {
            Flux<ServerSentEvent<String>> flux = chatService.streamChat(userId, sessionId, message, false);
            flux.doOnNext(event -> {
                        try {
                            boolean isError = forwardEvent(emitter, event, sessionId, replyHolder);
                            if (isError) {
                                errorSent.set(true);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .blockLast();

            String reply = replyHolder.get() == null ? "" : replyHolder.get().trim();
            if (!reply.isEmpty()) {
                sessionMetaService.touchSession(userId, sessionId, reply);
            }

            if (!errorSent.get()) {
                JSONObject meta = new JSONObject();
                meta.put("event", "APP_META");
                meta.put("sessionId", sessionId);
                meta.put("recipes", Collections.emptyList());
                meta.put("transferred", false);
                emitter.send(SseEmitter.event().data(meta.toJSONString()));
            }
            // 显式结束标记，便于客户端在网关半关闭前识别流结束
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            log.error("[SupportAi] 流式聊天失败 userId={} sessionId={}", userId, sessionId, e);
            sendError(emitter, e);
        }
    }

    /** @return true 表示已推送 APP_ERROR */
    private boolean forwardEvent(
            SseEmitter emitter,
            ServerSentEvent<String> event,
            String sessionId,
            AtomicReference<String> replyHolder) throws IOException {
        String eventName = event.event();
        String data = event.data();
        if (data == null || data.isBlank()) {
            return false;
        }

        if ("ERROR".equals(eventName)) {
            JSONObject err = new JSONObject();
            err.put("event", "APP_ERROR");
            err.put("message", extractErrorMessage(data));
            err.put("sessionId", sessionId);
            emitter.send(SseEmitter.event().data(err.toJSONString()));
            return true;
        }

        if ("TEXT_BLOCK_DELTA".equals(eventName)) {
            String delta = extractDelta(data);
            if (delta == null || delta.isEmpty()) {
                return false;
            }
            replyHolder.updateAndGet(prev -> (prev == null ? "" : prev) + delta);
            JSONObject payload = new JSONObject();
            payload.put("event", "MESSAGE");
            JSONObject inner = new JSONObject();
            inner.put("message", delta);
            payload.put("data", inner);
            payload.put("sessionId", sessionId);
            emitter.send(SseEmitter.event().data(payload.toJSONString()));
            return false;
        }
        return false;
    }

    private static String extractDelta(String json) {
        try {
            JSONObject obj = JSONObject.parseObject(json);
            return obj == null ? null : obj.getString("delta");
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractErrorMessage(String json) {
        try {
            JSONObject obj = JSONObject.parseObject(json);
            if (obj != null && obj.getString("message") != null) {
                return obj.getString("message");
            }
        } catch (Exception ignored) {
        }
        return "AI服务异常";
    }

    private void sendError(SseEmitter emitter, Exception e) {
        try {
            JSONObject err = new JSONObject();
            err.put("event", "APP_ERROR");
            err.put("message", e.getMessage() == null ? "AI服务异常" : e.getMessage());
            emitter.send(SseEmitter.event().data(err.toJSONString()));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }


    public List<AiMessageVO> listMessages(String authorization, String sessionId) {
        String userId = resolveUserId(authorization);
        if (sessionId == null || sessionId.isBlank()) {
            throw new JeecgBootException("sessionId 不能为空");
        }
        var history = chatService.getHistory(userId, sessionId, "display");
        List<AiMessageVO> result = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();
        int i = 0;
        for (var msg : history.getMessages()) {
            AiMessageVO vo = new AiMessageVO();
            vo.setId(sessionId + "-" + i);
            vo.setSessionId(sessionId);
            String role = msg.getRole() == null ? "" : msg.getRole().toLowerCase();
            vo.setSenderType("user".equals(role) ? "USER" : "AI");
            vo.setMsgType("text");
            vo.setContent(msg.getContent());
            vo.setCreateTime(now + i);
            vo.setRecipes(java.util.Collections.emptyList());
            result.add(vo);
            i++;
        }
        return result;
    }

    private String resolveUserId(String authorization) {
        Long userId = userResolver.resolveUserId(authorization);
        if (userId == null) {
            throw new JeecgBootException("用户未登录或Token无效");
        }
        return String.valueOf(userId);
    }
}
