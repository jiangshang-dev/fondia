package org.jeecg.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description: App用户第三方登录授权表
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Data
@TableName("fd_user_auth")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "App用户第三方登录授权表")
public class FdUserAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    /**用户id*/
    @Schema(description = "用户id")
    private Long userId;

    /**登录渠道类型: APPLE, GOOGLE, WECHAT, ALIPAY*/
    @Schema(description = "登录渠道类型: APPLE, GOOGLE, WECHAT, ALIPAY")
    private String identityType;

    /**第三方唯一标识 (微信的UnionID/OpenID, Google的Sub, Apple的SubjectID等)*/
    @Schema(description = "第三方唯一标识")
    private String identifier;

    /**授权凭证/Token (可选,如需要存三方返回的access_token或临时密匙)*/
    @Schema(description = "授权凭证/Token")
    private String credential;

    /**授权时同步的第三方平台昵称 (备用)*/
    @Schema(description = "授权时同步的第三方平台昵称")
    private String nickname;

    /**授权时同步的第三方平台头像 (备用)*/
    @Schema(description = "授权时同步的第三方平台头像")
    private String avatar;

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
