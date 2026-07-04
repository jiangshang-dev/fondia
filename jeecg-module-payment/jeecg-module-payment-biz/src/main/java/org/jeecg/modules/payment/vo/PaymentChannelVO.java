package org.jeecg.modules.payment.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentChannelVO {
    private String code;
    private String name;
    private String subtitle;
    private boolean defaultSelected;
}
