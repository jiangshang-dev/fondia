package org.jeecg.modules.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客服配置（App端）")
public class CsConfigVO {

    private String welcomeTitle;
    private String welcomeTag;
    private String welcomeDesc;
    private String humanGreeting;
    private String privacyTip;
    private String transferKeywords;
}
