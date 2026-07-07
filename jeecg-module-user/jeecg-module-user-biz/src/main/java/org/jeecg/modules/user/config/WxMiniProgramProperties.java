package org.jeecg.modules.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wx.mini")
public class WxMiniProgramProperties {
    private String appId;
    private String appSecret;
}
