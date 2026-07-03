package org.jeecg.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 注销账号验证信息预览
 */
@Data
@Schema(description = "注销账号验证信息预览")
public class AccountDeletePreviewVO {

    @Schema(description = "验证方式: phone / email")
    private String verifyType;

    @Schema(description = "脱敏后的联系方式")
    private String maskedContact;
}
