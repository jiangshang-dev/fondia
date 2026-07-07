package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "餐次详情")
public class FastMealItemVO {
    private String mealType;
    private String mealTypeLabel;
    private String name;
    private String description;
    private Integer kcal;
    private List<String> tags;
}
