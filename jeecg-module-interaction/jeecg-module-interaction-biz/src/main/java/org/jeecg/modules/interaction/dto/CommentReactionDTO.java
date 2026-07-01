package org.jeecg.modules.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论反应（点赞/点踩）
 */
@Data
@Schema(description = "评论反应请求")
public class CommentReactionDTO {

    @NotNull(message = "评论ID不能为空")
    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commentId;

    @NotNull(message = "反应类型不能为空")
    @Schema(description = "反应类型：1点赞 2点踩", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reactionType;
}
