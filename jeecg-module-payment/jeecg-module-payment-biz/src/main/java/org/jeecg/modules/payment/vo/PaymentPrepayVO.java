package org.jeecg.modules.payment.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentPrepayVO {
    private String orderNo;
    private String payChannel;
    /** 支付宝 App 支付 orderString */
    private String orderString;
    /** Apple / Google 内购商品 ID */
    private String productId;
    /** 是否模拟支付（开发环境） */
    private boolean mockPay;
}
