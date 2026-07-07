package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "周餐单")
public class FastWeeklyMealVO {
    private String planCode;
    private String planName;
    private String description;
    private Integer targetKcalMin;
    private Integer targetKcalMax;
    private List<FastMealDayVO> days;
}
