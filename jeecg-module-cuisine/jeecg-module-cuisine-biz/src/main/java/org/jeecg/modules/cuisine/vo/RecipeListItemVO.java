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
@Schema(description = "菜谱列表项")
public class RecipeListItemVO {

    @Schema(description = "菜谱ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "烹饪时长(分钟)")
    private Integer cookMinutes;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "标签")
    private List<String> tags;
}
