package org.jeecg.modules.fasting.enums;

import lombok.Getter;

@Getter
public enum FastingPhaseEnum {
    FASTING("FASTING", "断食中"),
    EATING("EATING", "进食中");

    private final String code;
    private final String label;

    FastingPhaseEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
