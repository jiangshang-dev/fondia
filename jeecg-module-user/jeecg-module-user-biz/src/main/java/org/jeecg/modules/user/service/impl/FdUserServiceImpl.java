package org.jeecg.modules.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.dto.EmailLoginDTO;
import org.jeecg.modules.user.dto.PasswordLoginDTO;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.entity.FdUserAuth;
import org.jeecg.modules.user.handler.ThirdPartyLoginHandler;
import org.jeecg.modules.user.mapper.FdUserMapper;
import org.jeecg.modules.user.service.IFdUserAuthService;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.service.IVerificationCodeService;
import org.jeecg.modules.user.vo.LoginResultVO;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: App用户表 Service实现
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Slf4j
@Service
public class FdUserServiceImpl extends ServiceImpl<FdUserMapper, FdUser> implements IFdUserService {

    @Autowired
    private IFdUserAuthService userAuthService;

    @Autowired
    private IVerificationCodeService verificationCodeService;

    private Map<String, ThirdPartyLoginHandler> handlerMap;

    @Autowired
    public void setHandlers(List<ThirdPartyLoginHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(ThirdPartyLoginHandler::getIdentityType, Function.identity()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResultVO thirdPartyLogin(ThirdPartyLoginDTO loginDTO) {
        String identityType = loginDTO.getIdentityType();
        
        // 1. 获取对应登录渠道的处理器
        ThirdPartyLoginHandler handler = handlerMap.get(identityType);
        if (handler == null) {
            throw new JeecgBootException("不支持的登录渠道: " + identityType);
        }

        // 2. 验证第三方授权凭证
        if (!handler.verifyCredential(loginDTO)) {
            throw new JeecgBootException("第三方授权验证失败");
        }

        // 3. 查询是否已存在该授权记录
        FdUserAuth existingAuth = userAuthService.getByIdentityTypeAndIdentifier(
                identityType, loginDTO.getIdentifier());

        FdUser user;
        boolean isNewUser = false;

        if (existingAuth != null) {
            // 4.1 已存在授权记录，直接获取用户信息
            user = this.getUserById(existingAuth.getUserId());
            if (user == null) {
                throw new JeecgBootException("用户不存在");
            }
            // 更新授权信息
            existingAuth.setCredential(loginDTO.getCredential());
            existingAuth.setNickname(loginDTO.getNickname());
            existingAuth.setAvatar(loginDTO.getAvatar());
            existingAuth.setUpdateTime(System.currentTimeMillis());
            userAuthService.updateById(existingAuth);
        } else {
            // 4.2 不存在授权记录，创建新用户和授权记录
            isNewUser = true;
            ThirdPartyLoginHandler.ThirdPartyUserInfo thirdPartyUserInfo = handler.getUserInfo(loginDTO);
            
            // 创建新用户
            user = handler.buildNewUser(loginDTO, thirdPartyUserInfo);
            this.save(user);

            // 创建授权记录
            FdUserAuth userAuth = handler.buildUserAuth(loginDTO, user.getId());
            userAuthService.save(userAuth);
        }

        // 5. 生成登录Token
        String token = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // 6. 构建返回结果
        return LoginResultVO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userInfo(convertToUserInfoVO(user))
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResultVO emailLogin(EmailLoginDTO loginDTO) {
        String email = loginDTO.getEmail();
        String code = loginDTO.getCode();

        // 1. 验证邮箱验证码
        if (!verificationCodeService.verifyEmailCode(email, "login", code)) {
            throw new JeecgBootException("验证码错误或已过期");
        }

        // 2. 查询用户是否存在
        FdUser user = getUserByEmail(email);

        String username = "用户" + System.currentTimeMillis();
        boolean isNewUser = false;
        if (user == null) {
            // 3. 用户不存在，创建新用户
            isNewUser = true;
            user = new FdUser();
            user.setEmail(email);
            user.setNickname(username);
            user.setUsername(username);
            String salt = oConvertUtils.randomGen(8);
            String passwordEncode = PasswordUtil.encrypt(username, "123456", salt);
            user.setPassword(passwordEncode);
            user.setStatus(1);
            user.setCreateTime(System.currentTimeMillis());
            user.setUpdateTime(System.currentTimeMillis());
            this.save(user);
        }

        // 4. 检查账号状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new JeecgBootException("账号已被禁用或已注销");
        }

        // 5. 生成登录Token
        String token = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // 6. 构建返回结果
        return LoginResultVO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userInfo(convertToUserInfoVO(user))
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public LoginResultVO passwordLogin(PasswordLoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String password = loginDTO.getPassword();

        // 1. 查询用户信息
        FdUser user = getUserByAccount(account);
        if (user == null) {
            throw new JeecgBootException("账号或密码错误");
        }

        // 2. 检查账号状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new JeecgBootException("账号已被禁用或已注销");
        }

        // 3. 验证密码
        if (user.getPassword() == null) {
            throw new JeecgBootException("该账号未设置密码，请使用其他方式登录");
        }

        // 这里使用BCrypt验证密码，实际项目中密码应该是加密存储的
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new JeecgBootException("账号或密码错误");
        }

        // 4. 生成登录Token
        String token = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // 5. 构建返回结果
        return LoginResultVO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userInfo(convertToUserInfoVO(user))
                .isNewUser(false)
                .build();
    }

    @Override
    public FdUser getUserById(Long userId) {
        return this.getById(userId);
    }

    @Override
    public FdUser getUserByAccount(String account) {
        // 支持用户名、手机号、邮箱登录
        LambdaQueryWrapper<FdUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(FdUser::getUsername, account)
                .or()
                .eq(FdUser::getPhone, account)
                .or()
                .eq(FdUser::getEmail, account)
        );
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 根据邮箱查询用户
     */
    private FdUser getUserByEmail(String email) {
        LambdaQueryWrapper<FdUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FdUser::getEmail, email);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 生成访问令牌
     * TODO: 实际项目中需要使用JWT或其他Token生成方案
     */
    private String generateToken(FdUser user) {
        // 这里简化处理，实际项目应该使用JWT
        return "token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    /**
     * 生成刷新令牌
     * TODO: 实际项目中需要使用JWT或其他Token生成方案
     */
    private String generateRefreshToken(FdUser user) {
        return "refreshToken_" + user.getId() + "_" + System.currentTimeMillis();
    }

    /**
     * 转换为用户信息VO
     */
    private UserInfoVO convertToUserInfoVO(FdUser user) {
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }
}
