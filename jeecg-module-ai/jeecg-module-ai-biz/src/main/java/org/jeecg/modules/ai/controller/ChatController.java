package org.jeecg.modules.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.ai.dto.AiChatDTO;
import org.jeecg.modules.ai.dto.ChatDtos;
import org.jeecg.modules.ai.dto.resp.SseResponse;
import org.jeecg.modules.ai.service.AgentService;
import org.jeecg.modules.ai.service.ChatService;
import org.jeecg.modules.ai.vo.AiMessageVO;
import org.jeecg.modules.ai.vo.AiSessionSummaryVO;
import org.jeecg.modules.ai.vo.AiSessionVO;
import org.jeecg.modules.ai.vo.MessageDataVO;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI对话(App)")
@RestController
@RequestMapping("/agent/ai")
public class ChatController {
    @Resource
    private ChatService chatService;

    @Operation(summary = "AI聊天（SSE流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SseResponse<MessageDataVO<Object>>> aiChat(@RequestHeader("Authorization") String authorization, @Validated @RequestBody AiChatDTO dto) {
        return chatService.chat(authorization, dto);
    }

    @Operation(summary = "AI对话历史会话列表")
    @GetMapping("/sessions")
    public Result<List<AiSessionSummaryVO>> historyList(@RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "ai_tab") String source, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.OK(chatService.historyList(authorization, source, pageNo, pageSize));
    }

    @Operation(summary = "新建AI对话会话")
    @PostMapping("/session/new")
    public Result<AiSessionVO> createSession(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "ai_tab") String source) {
        return Result.OK(chatService.createSession(authorization, source));
    }

    @Operation(summary = "获取当前AI对话会话")
    @GetMapping("/session/active")
    public Result<AiSessionVO> activeSession(@RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "ai_tab") String source) {
        return Result.OK(chatService.activeSession(authorization, source));
    }

    @Operation(summary = "AI会话历史消息")
    @GetMapping("/session/{id}/messages")
    public Result<List<AiMessageVO>> messagesById(@RequestHeader("Authorization") String authorization, @PathVariable("id") String id) {
        return Result.OK(chatService.messagesById(authorization, id));
    }

    @Operation(summary = "停止对话")
    @PostMapping("/chat/stop")
    public Result<String> stopChat(@RequestHeader("Authorization") String authorization, @RequestBody ChatDtos.StopChatRequest request) {
        chatService.stopChat(authorization, request);
        return Result.OK("ok");
    }
}
