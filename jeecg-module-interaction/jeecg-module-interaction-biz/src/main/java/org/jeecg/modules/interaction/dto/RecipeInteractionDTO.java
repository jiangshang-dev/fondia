package org.jeecg.modules.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜谱互动（收藏/点赞/浏览）
 */
@Data
@Schema(description = "菜谱互动请求")
public class RecipeInteractionDTO {

    @NotNull(message = "菜谱ID不能为空")
    @Schema(description = "菜谱ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recipeId;
}
