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

}
