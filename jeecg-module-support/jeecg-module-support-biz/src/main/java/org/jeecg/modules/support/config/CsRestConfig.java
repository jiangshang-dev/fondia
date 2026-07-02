package org.jeecg.modules.support.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 客服模块专用 RestTemplate，避免与 {@link org.jeecg.config.RestTemplateConfig} 的 restTemplate 冲突。
 */
@Configuration
public class CsRestConfig {

    @Bean("csLoadBalancedRestTemplate")
    @LoadBalanced
    public RestTemplate csLoadBalancedRestTemplate() {
        return new RestTemplate();
    }
}
