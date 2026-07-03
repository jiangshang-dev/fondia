package org.jeecg.modules.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 华为账号 OAuth 配置（在 application.yml 中配置 thirdparty.huawei.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "thirdparty.huawei")
public class HuaweiOAuthProperties {

    /** AGC 应用的 Client ID */
    private String clientId = "";

    /** AGC 应用的 Client Secret（仅服务端保存） */
    private String clientSecret = "";

    /** OAuth 回调地址，需与 AGC 配置一致；HarmonyOS 原生可留空 */
    private String redirectUri = "";

    private String tokenUrl = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";

    /**
     * 开发模式：未配置 clientSecret 时，直接解析客户端传来的 idToken（勿用于生产）
     */
    private boolean devMode = true;
}
