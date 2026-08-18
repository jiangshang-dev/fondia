package org.jeecg.modules.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型思考 / 工具调用步骤，供前端深度思考时间线回显。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThinkingStepVO {
    private String id;
    /** thinking | tool */
    private String type;
    private String title;
    private String content;
    /** running | done */
    private String status;
    private String toolCallName;
    /** 工具入参（字符串或 JSON） */
    private String input;
}
