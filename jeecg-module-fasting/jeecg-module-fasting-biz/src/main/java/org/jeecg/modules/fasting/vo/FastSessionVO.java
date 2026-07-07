package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会话信息")
public class FastSessionVO {
    private Long id;
    private String planCode;
    private String phase;
    private String startTime;
    private String endTime;
    private Integer durationSec;
    private String clientUuid;
}
