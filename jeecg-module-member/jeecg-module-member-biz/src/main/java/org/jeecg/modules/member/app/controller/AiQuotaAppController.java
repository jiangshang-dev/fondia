package org.jeecg.modules.member.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.constant.AiQuotaFeature;
import org.jeecg.modules.member.service.app.IAiQuotaAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.jeecg.modules.member.vo.AiQuotaStatusVO;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "App AI配额")
@RestController
@RequestMapping("/member/ai-quota")
public class AiQuotaAppController {

    @Resource
    private IAiQuotaAppService aiQuotaAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "查询今日配额")
    @GetMapping("/status")
    public Result<AiQuotaStatusVO> status(@RequestHeader("Authorization") String authorization,
                                          @RequestParam(defaultValue = AiQuotaFeature.AI_NUTRITION) String feature) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(aiQuotaAppService.getStatus(userId, feature));
    }

    @Operation(summary = "消耗一次配额（生成专属方案前调用）")
    @PostMapping("/consume")
    public Result<AiQuotaStatusVO> consume(@RequestHeader("Authorization") String authorization,
                                           @RequestParam(defaultValue = AiQuotaFeature.AI_NUTRITION) String feature) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(aiQuotaAppService.consume(userId, feature));
    }
}
