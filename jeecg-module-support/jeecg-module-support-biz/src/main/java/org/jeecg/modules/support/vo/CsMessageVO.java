package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "聊天消息")
public class CsMessageVO {

    private String id;
    private String sessionId;
    private String senderType;
    private String msgType;
    private String content;
    private Long createTime;
}
