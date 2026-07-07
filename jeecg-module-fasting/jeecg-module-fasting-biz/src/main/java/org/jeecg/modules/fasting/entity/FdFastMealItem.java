package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_meal_item")
@Schema(description = "减脂餐单餐次")
public class FdFastMealItem implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long planId;
    private Long dayId;
    private String mealType;
    private String name;
    private String description;
    private Integer kcal;
    private String tags;
    private Integer sortOrder;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
