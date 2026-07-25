package org.jeecg.modules.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.ai.dto.AiChatDTO;
import org.jeecg.modules.ai.dto.ChatDtos.StopChatRequest;
import org.jeecg.modules.ai.service.SupportAiChatService;
import org.jeecg.modules.ai.vo.AiMessageVO;
import org.jeecg.modules.ai.vo.AiSessionSummaryVO;
import org.jeecg.modules.ai.vo.AiSessionVO;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 服务接口，供 fondia_app AI 助手页调用。
 * 前缀 /agent/ai，经网关路由到 jeecg-agent。
 */
@Tag(name = "AI对话(App)")
@RestController
@RequestMapping("/agent/ai")
public class SupportAiController {

    @Resource
    private SupportAiChatService supportAiChatService;

    @Operation(summary = "AI聊天（SSE流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter aiChat(@RequestHeader("Authorization") String authorization,
                             @Validated @RequestBody AiChatDTO dto) {
        return supportAiChatService.chatStream(authorization, dto);
    }

    @Operation(summary = "AI对话历史会话列表")
    @GetMapping("/sessions")
    public Result<List<AiSessionSummaryVO>> aiSessions(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "ai_tab") String source,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.OK(supportAiChatService.listSessions(authorization, source, pageNo, pageSize));
    }

    @Operation(summary = "新建AI对话会话")
    @PostMapping("/session/new")
    public Result<AiSessionVO> newAiSession(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "ai_tab") String source) {
        return Result.OK(supportAiChatService.createSession(authorization, source));
    }

    @Operation(summary = "获取AI Tab进行中的会话")
    @GetMapping("/session/active")
    public Result<AiSessionVO> activeAiSession(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "ai_tab") String source) {
        return Result.OK(supportAiChatService.getActiveSession(authorization, source));
    }

    @Operation(summary = "AI会话历史消息")
    @GetMapping("/session/{id}/messages")
    public Result<List<AiMessageVO>> messages(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") String id) {
        return Result.OK(supportAiChatService.listMessages(authorization, id));
    }

    @Operation(summary = "停止AI生成")
    @PostMapping("/chat/stop")
    public Result<String> stopChat(
            @RequestHeader("Authorization") String authorization,
            @RequestBody StopChatRequest request) {
        supportAiChatService.stopChat(authorization, request);
        return Result.OK("ok");
    }
}
