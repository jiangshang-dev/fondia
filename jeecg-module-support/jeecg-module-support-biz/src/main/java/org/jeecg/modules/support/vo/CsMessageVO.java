package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "聊天消息")
public class CsMessageVO {

    private String id;
    private String sessionId;
    private String senderType;
    private String msgType;
    private String content;
    private Long createTime;
    @Schema(description = "AI 消息附带的菜谱卡片（历史回放）")
    private List<AiRecipeCardVO> recipes;
}
