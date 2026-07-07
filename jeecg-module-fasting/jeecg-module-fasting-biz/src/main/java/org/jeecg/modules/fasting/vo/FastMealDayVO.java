package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "单日餐单")
public class FastMealDayVO {
    private Integer dayOfWeek;
    private String dayLabel;
    private Integer dailyKcal;
    private String tip;
    private List<FastMealItemVO> meals;
}
