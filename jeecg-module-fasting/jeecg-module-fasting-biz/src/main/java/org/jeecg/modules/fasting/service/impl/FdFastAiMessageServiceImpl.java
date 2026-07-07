package org.jeecg.modules.fasting.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.AiChatConfig;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.fasting.dto.FastAiChatDTO;
import org.jeecg.modules.fasting.entity.FdFastAiMessage;
import org.jeecg.modules.fasting.mapper.FdFastAiMessageMapper;
import org.jeecg.modules.fasting.service.IFdFastAiMessageService;
import org.jeecg.modules.fasting.vo.FastAiChatVO;
import org.jeecg.modules.fasting.vo.FastAiSseEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class FdFastAiMessageServiceImpl extends ServiceImpl<FdFastAiMessageMapper, FdFastAiMessage>
        implements IFdFastAiMessageService {

    private static final String SYSTEM_PROMPT = """
            你是 Fast16 轻断食助手，专门解答 16+8、18:6 等间歇性断食与减脂饮食问题。
            回答简洁实用，使用中文；涉及疾病、用药或严重不适时提醒用户咨询医生。
            不提供具体医疗诊断，仅作生活方式参考。""";

    @Resource
    private IAIChatHandler aiChatHandler;
    @Resource
    private AiChatConfig aiChatConfig;

    @Override
    public FastAiChatVO chat(Long userId, FastAiChatDTO dto) {
        requireLogin(userId);
        if (aiChatConfig != null && !aiChatConfig.isEnabled()) {
            throw new JeecgBootException("AI 助手未启用，请检查 jeecg.ai-chat 配置");
        }
        String sessionId = resolveSessionId(dto.getSessionId());
        saveMessage(userId, sessionId, "user", dto.getQuestion());

        List<ChatMessage> messages = buildMessages(userId, sessionId, dto);
        AIChatParams params = buildChatParams();
        String answer = aiChatHandler.completionsByDefaultModel(messages, params);
        if (oConvertUtils.isEmpty(answer)) {
            throw new JeecgBootException("AI 未返回内容，请检查模型配置或稍后重试");
        }
        saveMessage(userId, sessionId, "assistant", answer);

        FastAiChatVO vo = new FastAiChatVO();
        vo.setSessionId(sessionId);
        vo.setAnswer(answer);
        return vo;
    }

    @Override
    public SseEmitter streamChat(Long userId, FastAiChatDTO dto) {
        requireLogin(userId);
        if (aiChatConfig != null && !aiChatConfig.isEnabled()) {
            throw new JeecgBootException("AI 助手未启用，请检查 jeecg.ai-chat 配置");
        }

        String sessionId = resolveSessionId(dto.getSessionId());
        saveMessage(userId, sessionId, "user", dto.getQuestion());

        List<ChatMessage> messages = buildMessages(userId, sessionId, dto);
        AIChatParams params = buildChatParams();

        SseEmitter emitter = new SseEmitter(0L);
        StringBuilder fullAnswer = new StringBuilder();
        AtomicBoolean thinking = new AtomicBoolean(false);

        try {
            TokenStream tokenStream = aiChatHandler.chatByDefaultModel(messages, params);
            tokenStream
                    .onPartialResponse(chunk -> {
                        String piece = normalizeStreamChunk(chunk, thinking);
                        if (oConvertUtils.isEmpty(piece)) {
                            return;
                        }
                        fullAnswer.append(piece);
                        sendEvent(emitter, FastAiSseEvent.builder()
                                .type("delta")
                                .content(piece)
                                .sessionId(sessionId)
                                .build());
                    })
                    .onCompleteResponse(response -> {
                        FinishReason reason = response.finishReason();
                        if (FinishReason.STOP.equals(reason) || reason == null) {
                            String answer = fullAnswer.toString();
                            if (oConvertUtils.isNotEmpty(answer)) {
                                saveMessage(userId, sessionId, "assistant", answer);
                            }
                            sendEvent(emitter, FastAiSseEvent.builder()
                                    .type("done")
                                    .sessionId(sessionId)
                                    .content(answer)
                                    .build());
                            completeEmitter(emitter);
                        } else {
                            String err = response.aiMessage() != null ? response.aiMessage().text() : "模型响应异常";
                            sendEvent(emitter, FastAiSseEvent.builder().type("error").message(err).build());
                            completeEmitter(emitter);
                        }
                    })
                    .onError(error -> {
                        log.error("[Fast16-AI] 流式调用失败", error);
                        sendEvent(emitter, FastAiSseEvent.builder()
                                .type("error")
                                .message("调用大模型失败：" + error.getMessage())
                                .build());
                        completeEmitter(emitter);
                    })
                    .start();
        } catch (Exception e) {
            log.error("[Fast16-AI] 启动流式对话失败", e);
            sendEvent(emitter, FastAiSseEvent.builder()
                    .type("error")
                    .message("调用大模型失败：" + e.getMessage())
                    .build());
            completeEmitter(emitter);
        }

        return emitter;
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new JeecgBootException("请先登录后使用 AI 助手");
        }
    }

    private String resolveSessionId(String sessionId) {
        return oConvertUtils.isNotEmpty(sessionId)
                ? sessionId
                : UUID.randomUUID().toString().replace("-", "");
    }

    private AIChatParams buildChatParams() {
        AIChatParams params = new AIChatParams();
        params.setTemperature(0.7);
        params.setTopP(0.9);
        if (aiChatConfig != null) {
            if (oConvertUtils.isNotEmpty(aiChatConfig.getModel())) {
                params.setModelName(aiChatConfig.getModel());
            }
            if (oConvertUtils.isNotEmpty(aiChatConfig.getApiKey())) {
                params.setApiKey(aiChatConfig.getApiKey());
            }
            if (oConvertUtils.isNotEmpty(aiChatConfig.getApiHost())) {
                params.setBaseUrl(aiChatConfig.getApiHost());
            }
            if (aiChatConfig.getTimeout() > 0) {
                params.setTimeout(aiChatConfig.getTimeout() * 1000);
            }
        }
        return params;
    }

    private List<ChatMessage> buildMessages(Long userId, String sessionId, FastAiChatDTO dto) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(dto)));

        List<FdFastAiMessage> history = list(new LambdaQueryWrapper<FdFastAiMessage>()
                .eq(FdFastAiMessage::getUserId, userId)
                .eq(FdFastAiMessage::getSessionId, sessionId)
                .orderByAsc(FdFastAiMessage::getCreateTime)
                .last("LIMIT 20"));

        for (FdFastAiMessage item : history) {
            if ("user".equalsIgnoreCase(item.getRole())) {
                messages.add(new UserMessage(item.getContent()));
            } else if ("assistant".equalsIgnoreCase(item.getRole())) {
                messages.add(new AiMessage(item.getContent()));
            }
        }
        return messages;
    }

    private String buildSystemPrompt(FastAiChatDTO dto) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);
        if (oConvertUtils.isNotEmpty(dto.getPlanCode())) {
            sb.append("\n用户当前计划：").append(dto.getPlanCode());
        }
        if (oConvertUtils.isNotEmpty(dto.getPhase())) {
            sb.append("，阶段：").append(dto.getPhase());
        }
        if (oConvertUtils.isNotEmpty(dto.getDurationText())) {
            sb.append("，已持续：").append(dto.getDurationText());
        }
        sb.append("。");
        return sb.toString();
    }

    private String normalizeStreamChunk(String chunk, AtomicBoolean thinking) {
        if (chunk == null) {
            return "";
        }
        if ("<think>".equals(chunk)) {
            thinking.set(true);
            return "";
        }
        if ("</think>".equals(chunk)) {
            thinking.set(false);
            return "\n";
        }
        if (thinking.get()) {
            return "";
        }
        return chunk;
    }

    private void sendEvent(SseEmitter emitter, FastAiSseEvent event) {
        try {
            emitter.send(SseEmitter.event().data(JSON.toJSONString(event)));
        } catch (IOException e) {
            log.warn("[Fast16-AI] SSE 发送失败: {}", e.getMessage());
        }
    }

    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void saveMessage(Long userId, String sessionId, String role, String content) {
        FdFastAiMessage message = new FdFastAiMessage();
        message.setUserId(userId);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(new Date());
        save(message);
    }
}
