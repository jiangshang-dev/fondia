package org.jeecg.modules.support.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TableName("fd_cs_session")
@Schema(description = "客服会话")
public class FdCsSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String agentId;
    private String status;
    private String source;
    private String aiConversationId;
    private String lastMessage;
    private Long lastMessageTime;
    private Integer userUnread;
    private Integer agentUnread;
    private Long createTime;
    private Long closeTime;
}
