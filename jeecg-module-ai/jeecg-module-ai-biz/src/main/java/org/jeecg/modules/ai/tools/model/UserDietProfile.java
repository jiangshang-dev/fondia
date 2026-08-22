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
public class UserDietProfile {
    private Long userId;
    private String tastePreference;
    private String dietaryPreference;
    private List<String> avoidIngredients;
    private List<String> favoriteCuisines;
    private List<String> kitchenEquipment;
    private Integer cookingLevel;
}
