package org.jeecg.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: App用户表
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Data
@TableName("fd_user")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "App用户表")
public class FdUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    /**用户名*/
    @Schema(description = "用户名")
    private String username;

    /**密码*/
    @Schema(description = "密码")
    private String password;

    /**密码盐*/
    @Schema(description = "密码盐")
    private String salt;

    /**用户昵称*/
    @Schema(description = "用户昵称")
    private String nickname;

    /**头像*/
    @Schema(description = "头像")
    private String avatar;

    /**绑定手机号*/
    @Schema(description = "绑定手机号")
    private String phone;

    /**绑定电子邮箱*/
    @Schema(description = "绑定电子邮箱")
    private String email;

    /**性别 (0:未知, 1:男, 2:女)*/
    @Schema(description = "性别 (0:未知, 1:男, 2:女)")
    private String gender;

    /**出生日期*/
    @Schema(description = "出生日期")
    private Date birthday;

    /**居住省份*/
    @Schema(description = "居住省份")
    private String province;

    /**居住城市*/
    @Schema(description = "居住城市")
    private String city;

    /**居住区/县*/
    @Schema(description = "居住区/县")
    private String district;

    /**个性签名*/
    @Schema(description = "个性签名")
    private String sign;

    /**厨艺等级 (如 1:初学者, 4:厨艺达人)*/
    @Schema(description = "厨艺等级 (如 1:初学者, 4:厨艺达人)")
    private Integer cookingLevel;

    /**口味偏好*/
    @Schema(description = "口味偏好")
    private String tastePreference;

    /**饮食偏好*/
    @Schema(description = "饮食偏好")
    private String dietaryPreference;

    /**消息通知 1开 0关*/
    @Schema(description = "消息通知 1开 0关")
    private Integer notifyEnabled;

    /**参与活动 1开 0关*/
    @Schema(description = "参与活动 1开 0关")
    private Integer activityEnabled;

    /**成长值*/
    @Schema(description = "成长值")
    private Integer growthValue;

    /**积分*/
    @Schema(description = "积分")
    private Integer points;

    /**连续签到天数*/
    @Schema(description = "连续签到天数")
    private Integer checkInDays;

    /**会员状态 (0:非会员, 1:有效会员, 2:已过期)*/
    @Schema(description = "会员状态 (0:非会员, 1:有效会员, 2:已过期)")
    private Integer memberStatus;

    /**会员到期时间(毫秒时间戳，冗余快照)*/
    @Schema(description = "会员到期时间(毫秒时间戳)")
    private Long memberExpireTime;

    /**当前生效套餐编码(冗余快照)*/
    @Schema(description = "当前生效套餐编码")
    private String memberPlanCode;

    /**账号状态 (1:正常, 0:禁用, -1:已注销)*/
    @Schema(description = "账号状态 (1:正常, 0:禁用, -1:已注销)")
    private Integer status;

    /**创建用户*/
    @Schema(description = "创建用户")
    private Integer createUser;

    /**创建时间*/
    @Schema(description = "创建时间")
    private Long createTime;

    /**更新用户*/
    @Schema(description = "更新用户")
    private Integer updateUser;

    /**更新时间*/
    @Schema(description = "更新时间")
    private Long updateTime;
}
