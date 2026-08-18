package org.jeecg.modules.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI会话消息（与 App AiExclusive 历史回放字段对齐）")
public class AiMessageVO {

    private String id;
    private String sessionId;
    private String senderType;
    private String msgType;
    private String content;
    private Long createTime;
    private List<?> recipes;
}
