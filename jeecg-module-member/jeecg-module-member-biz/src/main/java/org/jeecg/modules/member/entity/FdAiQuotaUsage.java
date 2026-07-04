package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_ai_quota_usage")
@Accessors(chain = true)
@Schema(description = "AI功能每日使用计数")
public class FdAiQuotaUsage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "功能编码")
    private String featureCode;

    @Schema(description = "使用日期 yyyyMMdd")
    private Integer usageDate;

    @Schema(description = "已使用次数")
    private Integer usedCount;

    private Date createTime;
    private Date updateTime;
}
