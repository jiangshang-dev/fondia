package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@TableName("fd_recipe")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱主表")
public class FdRecipe extends FdCuisineBaseEntity {

    @Schema(description = "所属子分类ID(L2)")
    private Long categoryId;

    @Schema(description = "所属菜系ID(L1)")
    private Long cuisineId;

    @Schema(description = "菜谱标题")
    private String title;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "详情描述")
    private String description;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频时长(秒)")
    private Integer videoDuration;

    @Schema(description = "难度：1简单 2中等 3困难")
    private Integer difficulty;

    @Schema(description = "烹饪时长(分钟)")
    private Integer cookMinutes;

    @Schema(description = "热量(大卡/100克)")
    private Integer calories;

    @Schema(description = "份量说明")
    private String servingSize;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态：0下架 1上架")
    private Integer status;

    @Schema(description = "发布时间")
    private Date publishTime;
}
