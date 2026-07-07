package org.jeecg.modules.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量同步会话")
public class FastSessionSyncDTO {
    private List<FastSessionSyncItemDTO> items;
}
