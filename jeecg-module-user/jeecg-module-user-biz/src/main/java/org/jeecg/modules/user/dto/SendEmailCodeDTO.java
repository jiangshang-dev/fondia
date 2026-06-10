package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱验证码DTO
 */
@Data
@Schema(description = "发送邮箱验证码DTO")
public class SendEmailCodeDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", required = true)
    private String email;

    @Schema(description = "场景: login-登录, register-注册, reset-重置密码", defaultValue = "login")
    private String scene = "login";
}
