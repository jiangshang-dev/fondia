package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_recipe_category")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱分类表")
public class FdRecipeCategory extends FdCuisineBaseEntity {

    @Schema(description = "父级ID，0表示顶级菜系")
    private Long parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "层级：1=菜系，2=子分类")
    private Integer categoryLevel;

    @Schema(description = "子分类类型：1=主食，2=菜")
    private Integer subType;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "特色标签JSON")
    private String featureTags;

    @Schema(description = "是否热门菜系")
    private Integer isHot;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}
