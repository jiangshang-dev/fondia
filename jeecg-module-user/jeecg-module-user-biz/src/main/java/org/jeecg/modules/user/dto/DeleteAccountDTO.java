package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注销账号请求
 */
@Data
@Schema(description = "注销账号请求")
public class DeleteAccountDTO {

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", required = true)
    private String code;
}
