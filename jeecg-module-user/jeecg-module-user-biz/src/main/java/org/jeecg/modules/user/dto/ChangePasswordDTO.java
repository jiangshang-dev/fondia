package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "修改密码")
public class ChangePasswordDTO {

    @Schema(description = "旧密码（未设置过密码时可不传）")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", required = true)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", required = true)
    private String confirmPassword;
}
