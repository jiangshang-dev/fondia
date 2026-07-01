package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "首页轮播图")
public class HomeBannerVO {

    @Schema(description = "轮播ID")
    private Long id;

    @Schema(description = "关联菜谱ID")
    private Long recipeId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "图片地址")
    private String imageUrl;
}
