package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI回复")
public class FastAiChatVO {
    private String sessionId;
    private String answer;
}
