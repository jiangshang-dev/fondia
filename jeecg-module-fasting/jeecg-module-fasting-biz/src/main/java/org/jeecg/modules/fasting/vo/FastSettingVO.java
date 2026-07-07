package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户设置")
public class FastSettingVO {
    private String planCode;
    private String eatStartTime;
    private Integer hapticEnabled;
    private String locale;
}
