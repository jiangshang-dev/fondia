package org.jeecg.modules.user.handler;

import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.entity.FdUserAuth;

/**
 * 第三方登录处理器接口
 * 每种登录方式(APPLE、GOOGLE、ALIPAY等)实现此接口
 */
public interface ThirdPartyLoginHandler {

    /**
     * 获取登录渠道类型
     * @return 登录渠道类型
     */
    String getIdentityType();

    /**
     * 验证第三方授权凭证
     * @param loginDTO 登录信息
     * @return 验证是否成功
     */
    boolean verifyCredential(ThirdPartyLoginDTO loginDTO);

    /**
     * 获取第三方用户信息(可选，部分平台可能需要调用接口获取)
     * @param loginDTO 登录信息
     * @return 第三方用户信息
     */
    default ThirdPartyUserInfo getUserInfo(ThirdPartyLoginDTO loginDTO) {
        return ThirdPartyUserInfo.builder()
                .nickname(loginDTO.getNickname())
                .avatar(loginDTO.getAvatar())
                .build();
    }

    /**
     * 构建用户授权信息
     * @param loginDTO 登录信息
     * @param userId 用户ID
     * @return 用户授权信息
     */
    default FdUserAuth buildUserAuth(ThirdPartyLoginDTO loginDTO, Long userId) {
        FdUserAuth userAuth = new FdUserAuth();
        userAuth.setUserId(userId);
        userAuth.setIdentityType(loginDTO.getIdentityType());
        userAuth.setIdentifier(loginDTO.getIdentifier());
        userAuth.setCredential(loginDTO.getCredential());
        userAuth.setNickname(loginDTO.getNickname());
        userAuth.setAvatar(loginDTO.getAvatar());
        userAuth.setCreateTime(System.currentTimeMillis());
        userAuth.setUpdateTime(System.currentTimeMillis());
        return userAuth;
    }

    /**
     * 构建新用户信息
     * @param loginDTO 登录信息
     * @param userInfo 第三方用户信息
     * @return 用户信息
     */
    default FdUser buildNewUser(ThirdPartyLoginDTO loginDTO, ThirdPartyUserInfo userInfo) {
        String username = "user_" + System.currentTimeMillis();
        String salt = oConvertUtils.randomGen(8);
        FdUser user = new FdUser();
        user.setUsername(username);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(username, oConvertUtils.randomGen(16), salt));
        user.setNickname(userInfo.getNickname() != null ? userInfo.getNickname() : "用户" + System.currentTimeMillis());
        user.setAvatar(userInfo.getAvatar());
        user.setPhone(loginDTO.getPhone());
        user.setEmail(loginDTO.getEmail());
        user.setStatus(1);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        return user;
    }

    /**
     * 第三方用户信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class ThirdPartyUserInfo {
        private String nickname;
        private String avatar;
        private String email;
        private String gender;
    }
}
