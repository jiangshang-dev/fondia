package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类页分区")
public class CategorySectionVO {

    @Schema(description = "子分类ID")
    private Long categoryId;

    @Schema(description = "分区标题")
    private String title;

    @Schema(description = "子分类类型：1主食 2菜")
    private Integer subType;

    @Schema(description = "菜谱列表")
    private List<RecipeListItemVO> recipes;
}
