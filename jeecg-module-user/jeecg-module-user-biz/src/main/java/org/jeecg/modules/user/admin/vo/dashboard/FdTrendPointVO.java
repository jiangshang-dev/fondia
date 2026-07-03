package org.jeecg.modules.user.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "趋势数据点")
public class FdTrendPointVO {

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "金额(可选)")
    private BigDecimal amount;
}
