package org.jeecg.modules.interaction.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentStatusEnum {

    HIDDEN(0, "审核未通过"),
    PUBLISHED(1, "已发布"),
    AUDITING(2, "审核中");

    private final int code;
    private final String label;

    public static CommentStatusEnum of(Integer code) {
        if (code == null) {
            return AUDITING;
        }
        for (CommentStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return AUDITING;
    }

    public static boolean isPublished(Integer status) {
        return status == null || status == PUBLISHED.code;
    }
}
