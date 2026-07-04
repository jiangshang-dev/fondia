package org.jeecg.modules.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI功能配额状态")
public class AiQuotaStatusVO {

    @Schema(description = "功能编码")
    private String featureCode;

    @Schema(description = "是否有效会员")
    private Boolean member;

    @Schema(description = "当前套餐编码")
    private String planCode;

    @Schema(description = "当前套餐名称")
    private String planName;

    @Schema(description = "每日上限，-1 表示不限")
    private Integer dailyLimit;

    @Schema(description = "今日已用")
    private Integer usedToday;

    @Schema(description = "今日剩余，-1 表示不限")
    private Integer remaining;

    @Schema(description = "是否还可使用")
    private Boolean available;
}
