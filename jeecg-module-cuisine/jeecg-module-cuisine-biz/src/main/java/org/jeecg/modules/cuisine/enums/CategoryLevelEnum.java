package org.jeecg.modules.cuisine.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryLevelEnum {

    CUISINE(1, "菜系"),
    SUB(2, "子分类");

    private final int code;
    private final String desc;
}
