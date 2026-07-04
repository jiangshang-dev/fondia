package org.jeecg.modules.payment.dto;

import lombok.Data;

@Data
public class PaymentPrepayDTO {
    private String orderNo;
    private String payChannel;
}
