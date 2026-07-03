package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@TableName("fd_payment_record")
@Accessors(chain = true)
@Schema(description = "支付流水")
public class FdPaymentRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "业务订单号")
    private String orderNo;

    @Schema(description = "订单类型")
    private String orderType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "支付金额")
    private java.math.BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "第三方交易号")
    private String externalTradeNo;

    @Schema(description = "回调原文")
    private String externalPayload;

    @Schema(description = "回调时间")
    private Long notifyTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

}
