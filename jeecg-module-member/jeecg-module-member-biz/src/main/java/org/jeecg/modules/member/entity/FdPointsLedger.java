package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_points_ledger")
@Accessors(chain = true)
@Schema(description = "积分流水")
public class FdPointsLedger implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "变动积分")
    private Integer changeAmount;

    @Schema(description = "变动后余额")
    private Integer balanceAfter;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务关联ID")
    private String bizId;

    @Schema(description = "展示标题")
    private String title;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人")
    private Long operatorId;

    @Schema(description = "创建时间")
    private Long createTime;

}
