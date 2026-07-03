package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方登录DTO
 */
@Data
@Schema(description = "第三方登录DTO")
public class ThirdPartyLoginDTO {

    @NotBlank(message = "登录渠道类型不能为空")
    @Schema(description = "登录渠道类型: APPLE, GOOGLE, WECHAT, HUAWEI, ALIPAY 等", required = true)
    private String identityType;

    @NotBlank(message = "第三方唯一标识不能为空")
    @Schema(description = "第三方唯一标识", required = true)
    private String identifier;

    @Schema(description = "授权凭证/Token")
    private String credential;

    @Schema(description = "第三方平台昵称")
    private String nickname;

    @Schema(description = "第三方平台头像")
    private String avatar;

    @Schema(description = "手机号(首次登录时可选)")
    private String phone;

    @Schema(description = "邮箱(首次登录时可选)")
    private String email;
}
