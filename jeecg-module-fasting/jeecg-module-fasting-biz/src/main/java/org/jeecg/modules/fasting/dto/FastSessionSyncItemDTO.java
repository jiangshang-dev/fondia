package org.jeecg.modules.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "同步会话项")
public class FastSessionSyncItemDTO {
    private String clientUuid;
    private String phase;
    private String planCode;
    private String startTime;
    private String endTime;
    private Integer durationSec;
}
