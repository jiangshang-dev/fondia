package org.jeecg.modules.user.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "排行榜项")
public class FdRankItemVO {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数值")
    private Long value;
}
