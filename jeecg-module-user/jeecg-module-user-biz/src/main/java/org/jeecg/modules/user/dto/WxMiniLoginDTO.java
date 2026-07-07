package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信小程序登录")
public class WxMiniLoginDTO {
    @NotBlank(message = "微信 code 不能为空")
    private String code;
    private String nickname;
    private String avatar;
    private String deviceId;
}
