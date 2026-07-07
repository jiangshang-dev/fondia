package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_meal_day")
@Schema(description = "减脂餐单日配置")
public class FdFastMealDay implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long planId;
    private Integer dayOfWeek;
    private String dayLabel;
    private Integer dailyKcal;
    private String tip;
    private Integer sortOrder;
    private Date createTime;
    private Date updateTime;
}
