package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_recipe_ingredient")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱食材表")
public class FdRecipeIngredient extends FdCuisineBaseEntity {

    @Schema(description = "菜谱ID")
    private Long recipeId;

    @Schema(description = "食材名称")
    private String name;

    @Schema(description = "用量")
    private String amount;

    @Schema(description = "食材图片")
    private String image;

    @Schema(description = "排序")
    private Integer sortNo;
}
