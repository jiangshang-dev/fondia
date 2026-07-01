package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_recipe_tag")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱标签字典")
public class FdRecipeTag extends FdCuisineBaseEntity {

    @Schema(description = "标签名")
    private String name;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}
