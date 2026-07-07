package org.jeecg.modules.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "开始会话")
public class FastSessionStartDTO {
    @NotBlank(message = "阶段不能为空")
    private String phase;
    private String planCode;
    private String clientUuid;
    private String startTime;
}
