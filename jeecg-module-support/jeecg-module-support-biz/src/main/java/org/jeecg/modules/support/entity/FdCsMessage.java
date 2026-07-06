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
@TableName("fd_cs_message")
@Schema(description = "客服消息")
public class FdCsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;
    private String senderType;
    private String senderId;
    private String msgType;
    private String content;
    /** AI 消息扩展数据 JSON，如 {"recipes":[...]} */
    private String extraJson;
    private Long createTime;
    private String clientMsgId;
}
