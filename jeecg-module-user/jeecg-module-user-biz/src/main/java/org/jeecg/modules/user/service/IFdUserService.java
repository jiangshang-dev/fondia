package org.jeecg.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.user.dto.EmailLoginDTO;
import org.jeecg.modules.user.dto.PasswordLoginDTO;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.dto.UpdateUserProfileDTO;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.vo.LoginResultVO;
import org.jeecg.modules.user.vo.UserInfoVO;

/**
 * @Description: App用户表 Service接口
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
public interface IFdUserService extends IService<FdUser> {

    /**
     * 第三方登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginResultVO thirdPartyLogin(ThirdPartyLoginDTO loginDTO);

    /**
     * 邮箱验证码登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginResultVO emailLogin(EmailLoginDTO loginDTO);

    /**
     * 账号密码登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginResultVO passwordLogin(PasswordLoginDTO loginDTO);

    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    FdUser getUserById(Long userId);

    /**
     * 根据账号获取用户信息（支持用户名、手机号、邮箱）
     * @param account 账号
     * @return 用户信息
     */
    FdUser getUserByAccount(String account);

    /**
     * 根据 Token 获取当前登录用户信息
     */
    UserInfoVO getUserInfo(String authorization);

    /**
     * 更新当前用户资料
     */
    UserInfoVO updateUserProfile(String authorization, UpdateUserProfileDTO dto);
}
