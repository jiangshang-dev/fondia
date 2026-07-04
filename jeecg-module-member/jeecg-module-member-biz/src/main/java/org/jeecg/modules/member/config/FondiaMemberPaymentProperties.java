package org.jeecg.modules.member.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fondia.payment")
public class FondiaMemberPaymentProperties {

    /** 支付微服务内部调用密钥 */
    private String internalSecret = "change-me-payment-internal";

    /** 是否允许 App 直接调用 confirmPay（仅开发环境） */
    private boolean allowClientConfirm = false;
}
