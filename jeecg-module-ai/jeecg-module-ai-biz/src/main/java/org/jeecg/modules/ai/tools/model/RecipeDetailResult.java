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
public class RecipeDetailResult {
    private Long recipeId;
    private String name;
    private String description;
    private String cuisineName;
    private Integer difficulty;
    private Integer cookMinutes;
    private Integer calories;
    private String servingSize;
    private List<String> tags;
    private List<RecipeIngredientItem> ingredients;
    private List<RecipeStepItem> steps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeIngredientItem {
        private String name;
        private String amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeStepItem {
        private Integer stepNo;
        private String instruction;
    }
}
