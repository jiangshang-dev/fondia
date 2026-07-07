package org.jeecg.modules.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "AI问答")
public class FastAiChatDTO {
    @NotBlank(message = "问题不能为空")
    private String question;
    private String sessionId;
    private String planCode;
    private String phase;
    private String durationText;
}
