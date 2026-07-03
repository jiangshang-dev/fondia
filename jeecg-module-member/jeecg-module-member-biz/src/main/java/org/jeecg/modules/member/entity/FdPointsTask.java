package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_points_task")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "积分任务")
public class FdPointsTask extends FdMemberAdminEntity {

    @Schema(description = "任务编码")
    private String taskCode;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "奖励积分")
    private Integer rewardPoints;

    @Schema(description = "触发类型")
    private String actionType;

    @Schema(description = "每日上限")
    private Integer dailyLimit;

    @Schema(description = "总次数上限")
    private Integer totalLimit;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "跳转链接")
    private String jumpUrl;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态")
    private Integer status;

}
