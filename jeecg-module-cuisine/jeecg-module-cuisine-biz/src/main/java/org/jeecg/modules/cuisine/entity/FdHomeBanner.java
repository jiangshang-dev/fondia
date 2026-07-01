package org.jeecg.modules.cuisine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_home_banner")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "首页轮播图")
public class FdHomeBanner extends FdCuisineBaseEntity {

    @Schema(description = "关联菜谱ID")
    private Long recipeId;

    @Schema(description = "轮播标题")
    private String title;

    @Schema(description = "轮播副标题/描述")
    private String subtitle;

    @Schema(description = "轮播图片")
    private String imageUrl;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}
