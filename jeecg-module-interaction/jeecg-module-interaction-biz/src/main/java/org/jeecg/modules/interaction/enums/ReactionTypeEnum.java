package org.jeecg.modules.interaction.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论反应类型
 */
@Getter
@AllArgsConstructor
public enum ReactionTypeEnum {

    LIKE(1, "点赞"),
    DISLIKE(2, "点踩");

    private final int code;
    private final String desc;

    public static ReactionTypeEnum of(int code) {
        for (ReactionTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("无效的反应类型: " + code);
    }
}
