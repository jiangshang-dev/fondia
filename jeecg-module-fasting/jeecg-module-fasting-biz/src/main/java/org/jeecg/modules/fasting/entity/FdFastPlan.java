package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_plan")
@Schema(description = "断食计划模板")
public class FdFastPlan implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String code;
    private String name;
    private Integer fastHours;
    private Integer eatHours;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
