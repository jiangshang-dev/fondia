package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI聊天响应")
public class AiChatVO {

    private String sessionId;
    private String reply;
    private Boolean transferred;
    private String humanGreeting;
    @Schema(description = "AI回复中匹配到的App内菜谱")
    private List<AiRecipeCardVO> recipes;
}
