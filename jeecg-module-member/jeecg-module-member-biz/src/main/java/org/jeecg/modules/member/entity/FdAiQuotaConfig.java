package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_ai_quota_config")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "AI功能配额全局配置")
public class FdAiQuotaConfig extends FdMemberAdminEntity {

    @Schema(description = "功能编码")
    private String featureCode;

    @Schema(description = "功能名称")
    private String featureName;

    @Schema(description = "非会员每日免费次数")
    private Integer freeDailyLimit;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
