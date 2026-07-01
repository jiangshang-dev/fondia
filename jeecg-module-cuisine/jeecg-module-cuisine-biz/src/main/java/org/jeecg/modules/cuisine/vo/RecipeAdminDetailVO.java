package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理端菜谱详情")
public class RecipeAdminDetailVO extends RecipeDetailVO {

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态：0下架 1上架")
    private Integer status;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds = new ArrayList<>();
}
