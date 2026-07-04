package org.jeecg.modules.member.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.modules.member.constant.AiQuotaFeature;
import org.jeecg.modules.member.service.app.IAiQuotaAppService;
import org.jeecg.modules.member.vo.AiQuotaAdminSummaryVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "AI专属方案配额管理")
@RestController
@RequestMapping("/sys/fd/ai/quota")
public class FdAiQuotaAdminController {

    @Resource
    private IAiQuotaAppService aiQuotaAppService;

    @Operation(summary = "配额汇总（免费次数 + 各套餐次数）")
    @GetMapping("/summary")
    public Result<AiQuotaAdminSummaryVO> summary(
            @RequestParam(defaultValue = AiQuotaFeature.AI_NUTRITION) String feature) {
        return Result.OK(aiQuotaAppService.getAdminSummary(feature));
    }

    @AutoLog(value = "保存AI免费配额", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "保存非会员免费次数")
    @PostMapping("/saveFree")
    public Result<String> saveFree(@RequestBody SaveFreeRequest body) {
        aiQuotaAppService.saveAdminConfig(
                body.getFeatureCode(),
                body.getFreeDailyLimit(),
                body.getStatus(),
                body.getRemark());
        return Result.OK("保存成功");
    }

    @AutoLog(value = "保存会员AI配额", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "批量保存各会员套餐每日次数")
    @PostMapping("/savePlans")
    public Result<String> savePlans(@RequestBody SavePlansRequest body) {
        aiQuotaAppService.savePlanLimits(body.getPlans());
        return Result.OK("保存成功");
    }

    @Data
    public static class SaveFreeRequest {
        private String featureCode = AiQuotaFeature.AI_NUTRITION;
        private Integer freeDailyLimit;
        private Integer status;
        private String remark;
    }

    @Data
    public static class SavePlansRequest {
        private List<AiQuotaAdminSummaryVO.PlanQuotaItemVO> plans;
    }
}
