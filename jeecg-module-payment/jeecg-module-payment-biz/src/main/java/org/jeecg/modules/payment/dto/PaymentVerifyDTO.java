package org.jeecg.modules.payment.dto;

import lombok.Data;

@Data
public class PaymentVerifyDTO {
    private String orderNo;
    private String payChannel;
    /** Apple receipt / Google purchaseToken / 支付宝 out_trade_no */
    private String receipt;
    private String productId;
}
