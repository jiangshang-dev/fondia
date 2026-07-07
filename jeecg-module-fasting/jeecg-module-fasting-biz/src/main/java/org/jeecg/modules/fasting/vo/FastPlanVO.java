package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "计划列表项")
public class FastPlanVO {
    private Long id;
    private String code;
    private String name;
    private Integer fastHours;
    private Integer eatHours;
    private String description;
}
