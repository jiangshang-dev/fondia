package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_member_subscription")
@Accessors(chain = true)
@Schema(description = "会员订阅")
public class FdMemberSubscription implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "套餐ID")
    private Long planId;

    @Schema(description = "套餐编码")
    private String planCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "生效开始")
    private Long startTime;

    @Schema(description = "到期时间")
    private Long expireTime;

    @Schema(description = "自动续费")
    private Integer autoRenew;

    @Schema(description = "开通渠道")
    private String sourceChannel;

    @Schema(description = "第三方订阅ID")
    private String externalSubId;

    @Schema(description = "最近订单ID")
    private Long lastOrderId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

}
