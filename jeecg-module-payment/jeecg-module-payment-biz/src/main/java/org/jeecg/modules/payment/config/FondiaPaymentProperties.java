package org.jeecg.modules.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "fondia.payment")
public class FondiaPaymentProperties {

    /** 支付服务与 member 服务内部通信密钥 */
    private String internalSecret = "change-me-payment-internal";

    /** 开发环境模拟支付（无真实 SDK 调起） */
    private boolean mockEnabled = true;

    private Alipay alipay = new Alipay();

    @Data
    public static class Alipay {
        private String appId;
        private String privateKey;
        private String alipayPublicKey;
        /** 支付宝网关，沙箱：https://openapi-sandbox.dl.alipaydev.com/gateway.do */
        private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
        private String notifyUrl;
        private String charset = "UTF-8";
        private String signType = "RSA2";
    }
}
