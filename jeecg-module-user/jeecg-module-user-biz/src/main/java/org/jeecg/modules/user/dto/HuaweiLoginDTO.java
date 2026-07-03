package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 华为账号一键登录 DTO
 */
@Data
@Schema(description = "华为账号一键登录DTO")
public class HuaweiLoginDTO {

    @NotBlank(message = "authorizationCode不能为空")
    @Schema(description = "华为 Account Kit 返回的授权码", required = true)
    private String authorizationCode;

    @Schema(description = "华为返回的 idToken（可选，用于解析 OpenID/UnionID）")
    private String idToken;

    @Schema(description = "防 CSRF 的 state")
    private String state;

    @Schema(description = "昵称（客户端 AuthorizationWithHuaweiID 获取）")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "手机号（若客户端已获取）")
    private String phone;
}
