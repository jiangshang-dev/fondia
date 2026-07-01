package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_recipe_step")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱步骤表")
public class FdRecipeStep extends FdCuisineBaseEntity {

    @Schema(description = "菜谱ID")
    private Long recipeId;

    @Schema(description = "步骤序号")
    private Integer stepNo;

    @Schema(description = "步骤图片")
    private String image;

    @Schema(description = "步骤说明")
    private String content;

    @Schema(description = "小贴士")
    private String tip;

    @Schema(description = "排序")
    private Integer sortNo;
}
