package org.jeecg.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "AI聊天请求")
public class AiChatDTO {

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "会话ID，首次可为空")
    private String sessionId;

    @Schema(description = "来源标签")
    private String source;
}
