package org.jeecg.modules.interaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 切换状态结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "切换状态结果")
public class ToggleResultVO {

    @Schema(description = "是否已激活（收藏/点赞）")
    private Boolean active;

    @Schema(description = "当前数量")
    private Long count;
}
