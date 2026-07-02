package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会话信息")
public class CsSessionVO {

    private String id;
    private String status;
    private String source;
    private String humanGreeting;
    private Long createTime;
}
