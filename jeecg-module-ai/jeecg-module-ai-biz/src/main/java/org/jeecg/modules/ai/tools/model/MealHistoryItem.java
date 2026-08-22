package org.jeecg.modules.ai.tools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealHistoryItem {
    private Long recipeId;
    private String recipeName;
    private String eatDate;
    private String mealType;
}
