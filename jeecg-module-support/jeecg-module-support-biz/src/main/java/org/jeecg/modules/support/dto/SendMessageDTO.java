package org.jeecg.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送聊天消息")
public class SendMessageDTO {

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String clientMsgId;
}
