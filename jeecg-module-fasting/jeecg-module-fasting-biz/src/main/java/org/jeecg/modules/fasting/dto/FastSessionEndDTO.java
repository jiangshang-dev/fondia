package org.jeecg.modules.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "结束会话")
public class FastSessionEndDTO {
    private Long sessionId;
    private String clientUuid;
    private String endTime;
}
