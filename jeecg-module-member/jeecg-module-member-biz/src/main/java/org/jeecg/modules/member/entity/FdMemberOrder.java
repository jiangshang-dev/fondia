package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_member_order")
@Accessors(chain = true)
@Schema(description = "会员订单")
public class FdMemberOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "套餐ID")
    private Long planId;

    @Schema(description = "套餐编码")
    private String planCode;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "购买天数")
    private Integer durationDays;

    @Schema(description = "应付金额")
    private java.math.BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "客户端平台")
    private String clientPlatform;

    @Schema(description = "客户端地区")
    private String clientRegion;

    @Schema(description = "下单IP")
    private String clientIp;

    @Schema(description = "订阅ID")
    private Long subscriptionId;

    @Schema(description = "支付时间")
    private Long paidTime;

    @Schema(description = "关单时间")
    private Long closeTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

}
