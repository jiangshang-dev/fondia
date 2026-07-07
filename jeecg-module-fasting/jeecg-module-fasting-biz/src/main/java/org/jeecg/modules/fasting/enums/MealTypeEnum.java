package org.jeecg.modules.fasting.enums;

import lombok.Getter;

@Getter
public enum MealTypeEnum {
    BREAKFAST("BREAKFAST", "早餐"),
    LUNCH("LUNCH", "午餐"),
    DINNER("DINNER", "晚餐");

    private final String code;
    private final String label;

    MealTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
