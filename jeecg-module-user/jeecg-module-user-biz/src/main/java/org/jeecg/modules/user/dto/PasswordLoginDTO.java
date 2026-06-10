package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 账号密码登录DTO
 */
@Data
@Schema(description = "账号密码登录DTO")
public class PasswordLoginDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号(用户名/手机号/邮箱)", required = true)
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;
}
