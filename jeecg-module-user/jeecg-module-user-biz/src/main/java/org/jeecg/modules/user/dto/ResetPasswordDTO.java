package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "重置密码")
public class ResetPasswordDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "手机号或邮箱", required = true)
    private String account;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", required = true)
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", required = true)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", required = true)
    private String confirmPassword;
}
