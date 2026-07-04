package org.jeecg.modules.payment.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(FondiaPaymentProperties.class)
public class PaymentAutoConfiguration {
}
