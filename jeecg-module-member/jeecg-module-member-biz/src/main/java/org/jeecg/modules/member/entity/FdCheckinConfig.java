package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_checkin_config")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "签到奖励配置")
public class FdCheckinConfig extends FdMemberAdminEntity {

    @Schema(description = "第几天(1-7)")
    private Integer dayIndex;

    @Schema(description = "奖励积分")
    private Integer rewardPoints;

    @Schema(description = "奖励成长值")
    private Integer rewardGrowth;

    @Schema(description = "状态")
    private Integer status;

}
