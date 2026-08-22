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
public class RecipeCandidate {
    private Long recipeId;
    private String name;
    private String cuisineName;
    private List<String> ingredients;
    private List<String> tags;
    private Integer cookMinutes;
    private Integer calories;
}
