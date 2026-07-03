package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送重置密码验证码")
public class SendResetPasswordCodeDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "手机号或邮箱", required = true)
    private String account;
}
