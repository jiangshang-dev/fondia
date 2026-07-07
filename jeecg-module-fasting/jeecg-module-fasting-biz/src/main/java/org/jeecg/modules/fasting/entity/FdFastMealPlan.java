package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_meal_plan")
@Schema(description = "减脂餐单方案")
public class FdFastMealPlan implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer targetKcalMin;
    private Integer targetKcalMax;
    private Integer isDefault;
    private Integer sortOrder;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
