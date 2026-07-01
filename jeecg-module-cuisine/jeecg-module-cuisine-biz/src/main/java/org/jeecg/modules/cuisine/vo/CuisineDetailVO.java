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
@Schema(description = "菜系详情头部")
public class CuisineDetailVO {

    @Schema(description = "菜系ID")
    private Long id;

    @Schema(description = "菜系名称")
    private String name;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "特色标签")
    private List<String> featureTags;

    @Schema(description = "可选标签筛选项")
    private List<TagBriefVO> filterTags;
}
