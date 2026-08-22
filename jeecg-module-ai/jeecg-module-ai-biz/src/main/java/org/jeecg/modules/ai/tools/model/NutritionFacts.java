package org.jeecg.modules.ai.tools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionFacts {
    private Long recipeId;
    private String recipeName;
    private String ingredientName;
    private Integer servings;
    private Integer calories;
    private Integer protein;
    private Integer fat;
    private Integer carbohydrate;
}
