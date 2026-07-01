package org.jeecg.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "更新用户资料")
public class UpdateUserProfileDTO {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别 0未知 1男 2女")
    private String gender;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "生日")
    private Date birthday;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "个性签名")
    private String sign;

    @Schema(description = "厨艺等级")
    private Integer cookingLevel;

    @Schema(description = "口味偏好")
    private String tastePreference;

    @Schema(description = "饮食偏好")
    private String dietaryPreference;

    @Schema(description = "消息通知 1开 0关")
    private Integer notifyEnabled;

    @Schema(description = "参与活动 1开 0关")
    private Integer activityEnabled;
}
