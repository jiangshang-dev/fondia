package org.jeecg.modules.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 回复评论
 */
@Data
@Schema(description = "回复评论请求")
public class ReplyCommentDTO {

    @NotNull(message = "父评论ID不能为空")
    @Schema(description = "父评论ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;

    @NotBlank(message = "回复内容不能为空")
    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
