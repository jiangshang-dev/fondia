package org.jeecg.modules.cuisine.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubTypeEnum {

    STAPLE(1, "主食"),
    DISH(2, "菜");

    private final int code;
    private final String desc;

    public static String titleOf(Integer subType) {
        if (subType == null) {
            return "";
        }
        for (SubTypeEnum value : values()) {
            if (value.code == subType) {
                return value.desc;
            }
        }
        return "";
    }
}
