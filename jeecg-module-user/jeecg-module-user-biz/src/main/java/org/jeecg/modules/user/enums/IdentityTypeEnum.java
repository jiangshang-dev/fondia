package org.jeecg.modules.user.enums;

import lombok.Getter;

/**
 * 登录渠道类型枚举
 */
@Getter
public enum IdentityTypeEnum {
    APPLE("APPLE", "Apple账号"),
    GOOGLE("GOOGLE", "Google账号"),
    WECHAT("WECHAT", "微信"),
    PHONE("PHONE", "手机号"),
    EMAIL("EMAIL", "邮箱"),
    ACCOUNT("ACCOUNT", "账号"),
    ALIPAY("ALIPAY", "支付宝");

    private final String code;
    private final String desc;

    IdentityTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IdentityTypeEnum fromCode(String code) {
        for (IdentityTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown identity type: " + code);
    }
}
