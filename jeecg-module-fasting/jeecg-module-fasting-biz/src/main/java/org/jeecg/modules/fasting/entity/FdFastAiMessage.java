package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_ai_message")
@Schema(description = "AI对话记录")
public class FdFastAiMessage implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String sessionId;
    private String role;
    private String content;
    private Date createTime;
}
