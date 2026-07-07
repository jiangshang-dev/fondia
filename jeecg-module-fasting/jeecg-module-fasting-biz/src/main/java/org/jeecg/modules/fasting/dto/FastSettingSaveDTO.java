package org.jeecg.modules.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "保存用户设置")
public class FastSettingSaveDTO {
    @NotBlank(message = "计划编码不能为空")
    private String planCode;
    private String eatStartTime;
    private Integer hapticEnabled;
    private String locale;
}
