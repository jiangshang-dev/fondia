package org.jeecg.modules.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评论
 */
@Data
@Schema(description = "发表评论请求")
public class AddCommentDTO {

    @NotNull(message = "菜谱ID不能为空")
    @Schema(description = "菜谱ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recipeId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
