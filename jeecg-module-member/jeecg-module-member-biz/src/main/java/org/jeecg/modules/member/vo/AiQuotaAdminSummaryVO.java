package org.jeecg.modules.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI配额管理汇总")
public class AiQuotaAdminSummaryVO {

    private Long configId;
    private String featureCode;
    private String featureName;
    private Integer freeDailyLimit;
    private Integer status;
    private String remark;
    private List<PlanQuotaItemVO> plans;

    @Data
    public static class PlanQuotaItemVO {
        private Long id;
        private String planCode;
        private String name;
        private Integer durationDays;
        private Integer aiNutritionDailyLimit;
        private Integer status;
    }
}
