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
@Schema(description = "分类页树形内容")
public class CategoryTreeVO {

    @Schema(description = "菜系ID")
    private Long cuisineId;

    @Schema(description = "菜系名称")
    private String cuisineName;

    @Schema(description = "分区列表（主食/菜）")
    private List<CategorySectionVO> sections;
}
