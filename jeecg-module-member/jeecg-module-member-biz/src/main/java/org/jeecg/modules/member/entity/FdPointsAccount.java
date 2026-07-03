package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_points_account")
@Accessors(chain = true)
@Schema(description = "积分账户")
public class FdPointsAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "可用积分")
    private Integer balance;

    @Schema(description = "冻结积分")
    private Integer frozen;

    @Schema(description = "累计获得")
    private Integer totalEarned;

    @Schema(description = "累计消耗")
    private Integer totalSpent;

    @Schema(description = "乐观锁")
    private Integer version;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

}
