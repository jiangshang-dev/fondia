package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI聊天响应")
public class AiChatVO {

    private String sessionId;
    private String reply;
    private Boolean transferred;
    private String humanGreeting;
}
