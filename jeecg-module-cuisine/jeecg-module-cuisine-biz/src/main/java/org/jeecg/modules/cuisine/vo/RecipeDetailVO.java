package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeecg.modules.cuisine.entity.FdRecipeIngredient;
import org.jeecg.modules.cuisine.entity.FdRecipeStep;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜谱详情")
public class RecipeDetailVO {

    @Schema(description = "菜谱ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "阿里云VOD视频ID（与 videoUrl 相同，便于客户端识别）")
    private String videoId;

    @Schema(description = "可播放的视频地址（由 videoId 解析）")
    private String playUrl;

    @Schema(description = "视频时长(秒)")
    private Integer videoDuration;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "烹饪时长(分钟)")
    private Integer cookMinutes;

    @Schema(description = "热量")
    private Integer calories;

    @Schema(description = "份量")
    private String servingSize;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "菜系ID")
    private Long cuisineId;

    @Schema(description = "菜系名称")
    private String cuisineName;

    @Schema(description = "子分类ID")
    private Long categoryId;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "食材")
    private List<FdRecipeIngredient> ingredients;

    @Schema(description = "步骤")
    private List<FdRecipeStep> steps;

    @Schema(description = "发布时间")
    private Date publishTime;
}
