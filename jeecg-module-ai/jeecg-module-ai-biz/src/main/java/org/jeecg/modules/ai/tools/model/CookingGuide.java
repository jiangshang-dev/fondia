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
public class CookingGuide {
    private Long recipeId;
    private String recipeName;
    private String method;
    private Integer cookMinutes;
    private List<String> equipment;
    private List<RecipeDetailResult.RecipeStepItem> steps;
}
