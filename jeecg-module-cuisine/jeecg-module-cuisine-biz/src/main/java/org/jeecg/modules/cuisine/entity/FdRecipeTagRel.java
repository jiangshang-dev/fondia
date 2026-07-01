package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_recipe_tag_rel")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱标签关联表")
public class FdRecipeTagRel extends FdCuisineBaseEntity {

    @Schema(description = "菜谱ID")
    private Long recipeId;

    @Schema(description = "标签ID")
    private Long tagId;
}
