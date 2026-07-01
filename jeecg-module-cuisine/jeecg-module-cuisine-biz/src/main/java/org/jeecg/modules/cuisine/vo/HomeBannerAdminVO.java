package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "首页轮播图管理列表项")
public class HomeBannerAdminVO {

    private Long id;
    private Long recipeId;
    private String recipeTitle;
    private String title;
    private String subtitle;
    private String imageUrl;
    private Integer sortNo;
    private Integer status;
    private Date createTime;
}
