package org.jeecg.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 菜谱点赞表
 */
@Data
@TableName("fd_recipe_like")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱点赞表")
public class FdRecipeLike extends FdInteractionBaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "菜谱ID")
    private Long recipeId;
}
