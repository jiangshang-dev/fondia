package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_checkin_record")
@Accessors(chain = true)
@Schema(description = "签到记录")
public class FdCheckinRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "签到日期")
    private java.time.LocalDate checkinDate;

    @Schema(description = "连续天数")
    private Integer streakDays;

    @Schema(description = "周期第几天")
    private Integer cycleDayIndex;

    @Schema(description = "获得积分")
    private Integer rewardPoints;

    @Schema(description = "获得成长值")
    private Integer rewardGrowth;

    @Schema(description = "签到时间")
    private Long createTime;

}
