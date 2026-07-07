package org.jeecg.modules.fasting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "日统计项")
public class FastDailyStatVO {
    private String statDate;
    private Integer targetFastSec;
    private Integer actualFastSec;
    private Integer completed;
}
