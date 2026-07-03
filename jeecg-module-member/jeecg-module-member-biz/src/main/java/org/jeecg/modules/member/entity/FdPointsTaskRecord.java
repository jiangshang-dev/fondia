package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_points_task_record")
@Accessors(chain = true)
@Schema(description = "积分任务完成记录")
public class FdPointsTaskRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务编码")
    private String taskCode;

    @Schema(description = "完成日期")
    private java.time.LocalDate completeDate;

    @Schema(description = "奖励积分")
    private Integer rewardPoints;

    @Schema(description = "业务引用")
    private String bizRef;

    @Schema(description = "完成时间")
    private Long createTime;

}
