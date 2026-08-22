package org.jeecg.modules.ai.tools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanRecord {
    private Long planId;
    private String title;
    private Integer days;
    private String startDate;
    private List<MealPlanDay> meals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealPlanDay {
        private String date;
        private Long breakfastRecipeId;
        private Long lunchRecipeId;
        private Long dinnerRecipeId;
    }
}
