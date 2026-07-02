package org.jeecg.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建/转人工会话")
public class SessionCreateDTO {

    @Schema(description = "来源：general/account/points等")
    private String source;
}
