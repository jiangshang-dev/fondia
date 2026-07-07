package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_daily_stat")
@Schema(description = "断食日统计")
public class FdFastDailyStat implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Date statDate;
    private Long userId;
    private String deviceId;
    private String planCode;
    private Integer targetFastSec;
    private Integer actualFastSec;
    private Integer completed;
    private Date createTime;
}
