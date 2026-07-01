package org.jeecg.modules.interaction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fondia.aliyun-green")
public class FondiaAliyunGreenProperties {

    /** 是否启用阿里云内容安全 */
    private boolean enabled = false;

    private String accessKeyId;

    private String accessKeySecret;

    /** 例如 green-cip.cn-shanghai.aliyuncs.com */
    private String endpoint = "green-cip.cn-shanghai.aliyuncs.com";
}
