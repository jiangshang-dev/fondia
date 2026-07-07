package org.jeecg.modules.fasting.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fasting.dto.FastAiChatDTO;
import org.jeecg.modules.fasting.service.IFdFastAiMessageService;
import org.jeecg.modules.fasting.util.FastingAppContext;
import org.jeecg.modules.fasting.vo.FastAiChatVO;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Fast16-AI")
@RestController
@RequestMapping("/fasting/app/ai")
public class FastingAiAppController {

    @Resource
    private IFdFastAiMessageService aiMessageService;
    @Resource
    private IFdUserService userService;

    @Operation(summary = "AI问答")
    @PostMapping("/chat")
    public Result<FastAiChatVO> chat(HttpServletRequest request, @Validated @RequestBody FastAiChatDTO dto) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        return Result.OK(aiMessageService.chat(userId, dto));
    }
}
