package org.jeecg.modules.support.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.support.dto.AiChatDTO;
import org.jeecg.modules.support.dto.SessionCreateDTO;
import org.jeecg.modules.support.entity.FdCsFaq;
import org.jeecg.modules.support.entity.FdCsQuickEntry;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.service.IFdCsFaqService;
import org.jeecg.modules.support.service.IFdCsQuickEntryService;
import org.jeecg.modules.support.service.IFdCsSessionService;
import org.jeecg.modules.support.vo.AiChatVO;
import org.jeecg.modules.support.vo.CsConfigVO;
import org.jeecg.modules.support.vo.CsMessageVO;
import org.jeecg.modules.support.vo.CsSessionVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "在线客服(App)")
@RestController
@RequestMapping("/support")
public class SupportController {

    @Resource
    private IFdCsConfigService configService;

    @Resource
    private IFdCsFaqService faqService;

    @Resource
    private IFdCsQuickEntryService quickEntryService;

    @Resource
    private IFdCsSessionService sessionService;

    @IgnoreAuth
    @Operation(summary = "获取客服页配置")
    @GetMapping("/config")
    public Result<CsConfigVO> config() {
        return Result.OK(configService.getAppConfig());
    }

    @IgnoreAuth
    @Operation(summary = "FAQ列表")
    @GetMapping("/faq/list")
    public Result<List<FdCsFaq>> faqList() {
        return Result.OK(faqService.listEnabled());
    }

    @IgnoreAuth
    @Operation(summary = "快捷入口列表")
    @GetMapping("/quick-entry/list")
    public Result<List<FdCsQuickEntry>> quickEntryList() {
        return Result.OK(quickEntryService.listEnabled());
    }

    @Operation(summary = "AI聊天")
    @PostMapping("/ai/chat")
    public Result<AiChatVO> aiChat(@RequestHeader("Authorization") String authorization,
                                   @Validated @RequestBody AiChatDTO dto) {
        return Result.OK(sessionService.chatWithAi(authorization, dto));
    }

    @Operation(summary = "转接人工客服")
    @PostMapping("/session/transfer")
    public Result<CsSessionVO> transfer(@RequestHeader("Authorization") String authorization,
                                        @RequestBody(required = false) SessionCreateDTO dto) {
        return Result.OK(sessionService.transferToHuman(authorization, dto));
    }

    @Operation(summary = "获取当前进行中的会话")
    @GetMapping("/session/active")
    public Result<CsSessionVO> activeSession(@RequestHeader("Authorization") String authorization) {
        return Result.OK(sessionService.getActiveSession(authorization));
    }

    @Operation(summary = "会话历史消息")
    @GetMapping("/session/{id}/messages")
    public Result<List<CsMessageVO>> messages(@RequestHeader("Authorization") String authorization,
                                                @PathVariable("id") Long id,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.OK(sessionService.listMessages(authorization, id, pageNo, pageSize));
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/session/{id}/close")
    public Result<String> close(@RequestHeader("Authorization") String authorization,
                                @PathVariable("id") Long id) {
        sessionService.closeSession(authorization, id);
        return Result.OK("会话已关闭");
    }
}
