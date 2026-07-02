package org.jeecg.modules.vo;

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
@Schema(description = "菜谱搜索结果项")
public class RecipeSearchItemVO {

    private String id;
    private String title;
    private String subtitle;
    private String coverImage;
    private String cuisineName;
    private Integer difficulty;
    private Integer cookMinutes;
    private Integer likeCount;
    private List<String> tags;
}
