package org.jeecg.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息VO")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "绑定手机号")
    private String phone;

    @Schema(description = "绑定电子邮箱")
    private String email;

    @Schema(description = "性别 (0:未知, 1:男, 2:女)")
    private String gender;

    @Schema(description = "出生日期")
    private Date birthday;

    @Schema(description = "居住省份")
    private String province;

    @Schema(description = "居住城市")
    private String city;

    @Schema(description = "居住区/县")
    private String district;

    @Schema(description = "个性签名")
    private String sign;

    @Schema(description = "厨艺等级")
    private Integer cookingLevel;

    @Schema(description = "账号状态")
    private Integer status;
}
