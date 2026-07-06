package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI推荐菜谱卡片")
public class AiRecipeCardVO {

    private String id;
    private String title;
    private String subtitle;
    private String coverImage;
    private Integer cookMinutes;
    @Schema(description = "App 内菜谱详情跳转链接，如 fondia://recipe/{id}")
    private String url;
}
