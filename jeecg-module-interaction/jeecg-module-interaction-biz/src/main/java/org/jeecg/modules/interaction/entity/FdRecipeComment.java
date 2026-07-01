package org.jeecg.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 菜谱评论表
 */
@Data
@TableName("fd_recipe_comment")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱评论表")
public class FdRecipeComment extends FdInteractionBaseEntity {

    @Schema(description = "菜谱ID")
    private Long recipeId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "父评论ID，0为一级评论")
    private Long parentId;

    @Schema(description = "根评论ID")
    private Long rootId;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "点踩数")
    private Integer dislikeCount;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "评论IP")
    private String ip;

    @Schema(description = "状态：0隐藏 1正常 2审核中")
    private Integer status;
}
