package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客服会话(管理端)")
public class CsSessionAdminVO {

    private String id;
    private String userId;
    private String agentId;
    private String status;
    private String source;
    private String lastMessage;
    private Long lastMessageTime;
    private Integer userUnread;
    private Integer agentUnread;
    private Long createTime;
    private Long closeTime;
}
