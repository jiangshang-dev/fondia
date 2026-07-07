package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_user_setting")
@Schema(description = "用户断食设置")
public class FdFastUserSetting implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String deviceId;
    private String planCode;
    private String eatStartTime;
    private Integer hapticEnabled;
    private String locale;
    private Date createTime;
    private Date updateTime;
}
