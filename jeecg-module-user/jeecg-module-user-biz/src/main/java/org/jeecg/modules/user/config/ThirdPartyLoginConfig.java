package org.jeecg.modules.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方登录配置类
 * 实际项目中可以在这里配置各平台的AppID、AppSecret等
 */
@Configuration
@ConfigurationProperties(prefix = "thirdparty")
public class ThirdPartyLoginConfig {

    // TODO: 实际项目中可以通过配置文件读取各平台的配置信息
    // 例如:
    // @Value("${thirdparty.alipay.appId}")
    // private String alipayAppId;
    //
    // @Value("${thirdparty.alipay.privateKey}")
    // private String alipayPrivateKey;
    //
    // @Value("${thirdparty.alipay.publicKey}")
    // private String alipayPublicKey;
    //
    // @Value("${thirdparty.google.clientId}")
    // private String googleClientId;
    //
    // @Value("${thirdparty.apple.teamId}")
    // private String appleTeamId;
    //
    // @Value("${thirdparty.apple.bundleId}")
    // private String appleBundleId;
    //
    // @Value("${thirdparty.apple.keyId}")
    // private String appleKeyId;
    //
    // @Value("${thirdparty.wechat.appId}")
    // private String wechatAppId;
    //
    // @Value("${thirdparty.wechat.appSecret}")
    // private String wechatAppSecret;
}
