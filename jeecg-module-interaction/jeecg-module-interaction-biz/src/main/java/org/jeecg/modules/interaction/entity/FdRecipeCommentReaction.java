package org.jeecg.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 评论点赞点踩表
 */
@Data
@TableName("fd_recipe_comment_reaction")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "评论点赞点踩表")
public class FdRecipeCommentReaction extends FdInteractionBaseEntity {

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "反应类型：1点赞 2点踩")
    private Integer reactionType;
}
