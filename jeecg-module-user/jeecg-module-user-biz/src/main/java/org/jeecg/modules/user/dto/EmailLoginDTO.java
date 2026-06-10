package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 邮箱验证码登录DTO
 */
@Data
@Schema(description = "邮箱验证码登录DTO")
public class EmailLoginDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", required = true)
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", required = true)
    private String code;
}
