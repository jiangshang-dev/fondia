package org.jeecg.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.dto.ChangePasswordDTO;
import org.jeecg.modules.user.dto.EmailLoginDTO;
import org.jeecg.modules.user.dto.HuaweiLoginDTO;
import org.jeecg.modules.user.dto.PasswordLoginDTO;
import org.jeecg.modules.user.dto.ResetPasswordDTO;
import org.jeecg.modules.user.dto.SendResetPasswordCodeDTO;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.dto.UpdateUserProfileDTO;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.entity.FdUserAuth;
import org.jeecg.modules.user.config.HuaweiOAuthProperties;
import org.jeecg.modules.user.enums.IdentityTypeEnum;
import org.jeecg.modules.user.handler.ThirdPartyLoginHandler;
import org.jeecg.modules.user.mapper.FdUserMapper;
import org.jeecg.modules.user.service.HuaweiOAuthClient;
import org.jeecg.modules.user.service.IFdUserAuthService;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.service.IVerificationCodeService;
import org.jeecg.modules.user.util.HuaweiIdTokenUtil;
import org.jeecg.modules.user.vo.AccountDeletePreviewVO;
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

    private static final String DELETE_ACCOUNT_SCENE = "delete_account";
    private static final String RESET_PASSWORD_SCENE = "reset_password";
    /** 邮箱验证码注册时的默认登录密码 */
    private static final String DEFAULT_EMAIL_PASSWORD = "123456";

    @Autowired
    private IFdUserAuthService userAuthService;

    @Autowired
    private IVerificationCodeService verificationCodeService;

    @Autowired
    private HuaweiOAuthClient huaweiOAuthClient;

    @Autowired
    private HuaweiOAuthProperties huaweiOAuthProperties;

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
            // 3. 用户不存在，创建新用户（默认密码 123456，支持后续邮箱+密码登录）
            isNewUser = true;
            user = new FdUser();
            user.setEmail(email);
            user.setNickname(System.currentTimeMillis()+"");
            user.setUsername(username);
            user.setStatus(1);
            user.setCreateTime(System.currentTimeMillis());
            user.setUpdateTime(System.currentTimeMillis());
            setPlainPassword(user, DEFAULT_EMAIL_PASSWORD);
            this.save(user);
        } else if (!hasPassword(user)) {
            // 兼容历史邮箱用户：补全默认密码以便生成 Token
            setPlainPassword(user, DEFAULT_EMAIL_PASSWORD);
            user.setUpdateTime(System.currentTimeMillis());
            updateById(user);
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
        if (!hasPassword(user)) {
            throw new JeecgBootException("该账号未设置密码，请使用验证码登录后在设置中修改密码");
        }
        if (!matchesPassword(user, password)) {
            throw new JeecgBootException("账号或密码错误");
        }

        // 4. 生成登录Token（与邮箱登录一致）
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
    @Transactional(rollbackFor = Exception.class)
    public LoginResultVO huaweiLogin(HuaweiLoginDTO loginDTO) {
        String idToken = loginDTO.getIdToken();
        String accessToken = null;

        boolean hasSecret = oConvertUtils.isNotEmpty(huaweiOAuthProperties.getClientSecret())
                && oConvertUtils.isNotEmpty(huaweiOAuthProperties.getClientId());

        if (hasSecret) {
            HuaweiOAuthClient.HuaweiTokenResult tokenResult =
                    huaweiOAuthClient.exchangeAuthorizationCode(loginDTO.getAuthorizationCode());
            if (oConvertUtils.isNotEmpty(tokenResult.getIdToken())) {
                idToken = tokenResult.getIdToken();
            }
            accessToken = tokenResult.getAccessToken();
        } else if (huaweiOAuthProperties.isDevMode() && oConvertUtils.isNotEmpty(idToken)) {
            log.warn("华为 OAuth 未配置密钥，开发模式直接使用客户端 idToken，生产环境请配置 thirdparty.huawei.clientSecret");
        } else {
            throw new JeecgBootException("华为登录服务未配置，请联系管理员");
        }

        String unionId = HuaweiIdTokenUtil.getUnionId(idToken);
        String openId = HuaweiIdTokenUtil.getSub(idToken);
        String identifier = oConvertUtils.isNotEmpty(unionId) ? unionId : openId;
        if (oConvertUtils.isEmpty(identifier)) {
            throw new JeecgBootException("无法解析华为账号标识，请重试");
        }

        ThirdPartyLoginDTO thirdPartyLoginDTO = new ThirdPartyLoginDTO();
        thirdPartyLoginDTO.setIdentityType(IdentityTypeEnum.HUAWEI.getCode());
        thirdPartyLoginDTO.setIdentifier(identifier);
        thirdPartyLoginDTO.setCredential(accessToken != null ? accessToken : loginDTO.getAuthorizationCode());
        thirdPartyLoginDTO.setNickname(loginDTO.getNickname());
        thirdPartyLoginDTO.setAvatar(loginDTO.getAvatar());
        thirdPartyLoginDTO.setPhone(loginDTO.getPhone());

        return thirdPartyLogin(thirdPartyLoginDTO);
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

    @Override
    public UserInfoVO getUserInfo(String authorization) {
        String token = extractBearerToken(authorization);
        if (oConvertUtils.isEmpty(token)) {
            throw new JeecgBootException("Token不能为空");
        }

        String username = JwtUtil.getUsername(token);
        if (oConvertUtils.isEmpty(username)) {
            throw new JeecgBootException("Token无效");
        }

        FdUser user = resolveUserFromJwtUsername(username);
        if (user == null) {
            throw new JeecgBootException("用户不存在");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new JeecgBootException("账号已被禁用或已注销");
        }

        if (!JwtUtil.verify(token, username, user.getPassword())) {
            throw new JeecgBootException("Token无效或已过期");
        }

        return convertToUserInfoVO(user);
    }

    @Override
    public UserInfoVO updateUserProfile(String authorization, UpdateUserProfileDTO dto) {
        String token = extractBearerToken(authorization);
        if (oConvertUtils.isEmpty(token)) {
            throw new JeecgBootException("Token不能为空");
        }

        String username = JwtUtil.getUsername(token);
        FdUser user = resolveUserFromJwtUsername(username);
        if (user == null) {
            throw new JeecgBootException("用户不存在");
        }
        if (!JwtUtil.verify(token, username, user.getPassword())) {
            throw new JeecgBootException("Token无效或已过期");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            user.setBirthday(dto.getBirthday());
        }
        if (dto.getProvince() != null) {
            user.setProvince(dto.getProvince());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getDistrict() != null) {
            user.setDistrict(dto.getDistrict());
        }
        if (dto.getSign() != null) {
            user.setSign(dto.getSign());
        }
        if (dto.getCookingLevel() != null) {
            user.setCookingLevel(dto.getCookingLevel());
        }
        if (dto.getTastePreference() != null) {
            user.setTastePreference(dto.getTastePreference());
        }
        if (dto.getDietaryPreference() != null) {
            user.setDietaryPreference(dto.getDietaryPreference());
        }
        if (dto.getNotifyEnabled() != null) {
            user.setNotifyEnabled(dto.getNotifyEnabled());
        }
        if (dto.getActivityEnabled() != null) {
            user.setActivityEnabled(dto.getActivityEnabled());
        }
        user.setUpdateTime(System.currentTimeMillis());
        updateById(user);
        return convertToUserInfoVO(user);
    }

    @Override
    public AccountDeletePreviewVO getAccountDeletePreview(String authorization) {
        FdUser user = requireActiveUser(authorization);
        AccountDeletePreviewVO preview = new AccountDeletePreviewVO();
        if (oConvertUtils.isNotEmpty(user.getPhone())) {
            preview.setVerifyType("phone");
            preview.setMaskedContact(maskPhone(user.getPhone()));
            return preview;
        }
        if (oConvertUtils.isNotEmpty(user.getEmail())) {
            preview.setVerifyType("email");
            preview.setMaskedContact(maskEmail(user.getEmail()));
            return preview;
        }
        throw new JeecgBootException("请先绑定手机号或邮箱后再注销账号");
    }

    @Override
    public void sendAccountDeleteCode(String authorization) {
        FdUser user = requireActiveUser(authorization);
        if (oConvertUtils.isNotEmpty(user.getPhone())) {
            verificationCodeService.sendPhoneCode(user.getPhone(), DELETE_ACCOUNT_SCENE);
            return;
        }
        if (oConvertUtils.isNotEmpty(user.getEmail())) {
            verificationCodeService.sendEmailCode(user.getEmail(), DELETE_ACCOUNT_SCENE);
            return;
        }
        throw new JeecgBootException("请先绑定手机号或邮箱后再注销账号");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(String authorization, String code) {
        FdUser user = requireActiveUser(authorization);
        boolean verified = false;
        if (oConvertUtils.isNotEmpty(user.getPhone())) {
            verified = verificationCodeService.verifyAndConsumePhoneCode(
                    user.getPhone(), DELETE_ACCOUNT_SCENE, code);
        } else if (oConvertUtils.isNotEmpty(user.getEmail())) {
            verified = verificationCodeService.verifyAndConsumeEmailCode(
                    user.getEmail(), DELETE_ACCOUNT_SCENE, code);
        } else {
            throw new JeecgBootException("请先绑定手机号或邮箱后再注销账号");
        }
        if (!verified) {
            throw new JeecgBootException("验证码错误或已过期");
        }

        long now = System.currentTimeMillis();
        user.setStatus(-1);
        user.setNickname("已注销用户");
        user.setAvatar(null);
        user.setSign(null);
        user.setPhone(null);
        user.setEmail(null);
        user.setUsername("deleted_" + user.getId() + "_" + now);
        setPlainPassword(user, oConvertUtils.randomGen(16));
        user.setPoints(0);
        user.setGrowthValue(0);
        user.setCheckInDays(0);
        user.setUpdateTime(now);
        updateById(user);

        userAuthService.remove(new LambdaQueryWrapper<FdUserAuth>()
                .eq(FdUserAuth::getUserId, user.getId()));
    }

    @Override
    public void changePassword(String authorization, ChangePasswordDTO dto) {
        FdUser user = requireActiveUser(authorization);
        validateNewPassword(dto.getNewPassword(), dto.getConfirmPassword());
        if (hasPassword(user)) {
            if (oConvertUtils.isEmpty(dto.getOldPassword())) {
                throw new JeecgBootException("请输入旧密码");
            }
            if (!matchesPassword(user, dto.getOldPassword())) {
                throw new JeecgBootException("旧密码输入错误");
            }
            if (dto.getOldPassword().equals(dto.getNewPassword())) {
                throw new JeecgBootException("新密码不能与旧密码相同");
            }
        }
        setPlainPassword(user, dto.getNewPassword());
        user.setUpdateTime(System.currentTimeMillis());
        updateById(user);
    }

    @Override
    public void sendResetPasswordCode(SendResetPasswordCodeDTO dto) {
        FdUser user = requireResetPasswordUser(dto.getAccount());
        sendVerifyCodeForUser(user, RESET_PASSWORD_SCENE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        FdUser user = requireResetPasswordUser(dto.getAccount());
        validateNewPassword(dto.getNewPassword(), dto.getConfirmPassword());
        if (!verifyResetCode(user, dto.getCode())) {
            throw new JeecgBootException("验证码错误或已过期");
        }
        setPlainPassword(user, dto.getNewPassword());
        user.setUpdateTime(System.currentTimeMillis());
        updateById(user);
    }

    private FdUser requireResetPasswordUser(String account) {
        if (oConvertUtils.isEmpty(account)) {
            throw new JeecgBootException("账号不能为空");
        }
        FdUser user = getUserByAccount(account.trim());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new JeecgBootException("账号不存在或不可用");
        }
        return user;
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (oConvertUtils.isEmpty(newPassword)) {
            throw new JeecgBootException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new JeecgBootException("密码长度不能少于6位");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new JeecgBootException("两次输入密码不一致");
        }
    }

    private void sendVerifyCodeForUser(FdUser user, String scene) {
        if (oConvertUtils.isNotEmpty(user.getPhone())) {
            verificationCodeService.sendPhoneCode(user.getPhone(), scene);
            return;
        }
        if (oConvertUtils.isNotEmpty(user.getEmail())) {
            verificationCodeService.sendEmailCode(user.getEmail(), scene);
            return;
        }
        throw new JeecgBootException("账号未绑定手机号或邮箱，无法发送验证码");
    }

    private boolean verifyResetCode(FdUser user, String code) {
        if (oConvertUtils.isNotEmpty(user.getPhone())
                && verificationCodeService.verifyAndConsumePhoneCode(user.getPhone(), RESET_PASSWORD_SCENE, code)) {
            return true;
        }
        if (oConvertUtils.isNotEmpty(user.getEmail())) {
            return verificationCodeService.verifyAndConsumeEmailCode(user.getEmail(), RESET_PASSWORD_SCENE, code);
        }
        return false;
    }

    private boolean hasPassword(FdUser user) {
        return oConvertUtils.isNotEmpty(user.getPassword()) && oConvertUtils.isNotEmpty(user.getSalt());
    }

    private boolean matchesPassword(FdUser user, String plainPassword) {
        if (!hasPassword(user)) {
            return false;
        }
        String encoded = PasswordUtil.encrypt(resolveJwtUsername(user), plainPassword, user.getSalt());
        return user.getPassword().equals(encoded);
    }

    private void setPlainPassword(FdUser user, String plainPassword) {
        String salt = oConvertUtils.randomGen(8);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(resolveJwtUsername(user), plainPassword, salt));
    }

    /**
     * 解析并校验当前登录用户
     */
    private FdUser requireActiveUser(String authorization) {
        String token = extractBearerToken(authorization);
        if (oConvertUtils.isEmpty(token)) {
            throw new JeecgBootException("Token不能为空");
        }
        String username = JwtUtil.getUsername(token);
        if (oConvertUtils.isEmpty(username)) {
            throw new JeecgBootException("Token无效");
        }
        FdUser user = resolveUserFromJwtUsername(username);
        if (user == null) {
            throw new JeecgBootException("用户不存在");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new JeecgBootException("账号已被禁用或已注销");
        }
        if (!JwtUtil.verify(token, username, user.getPassword())) {
            throw new JeecgBootException("Token无效或已过期");
        }
        return user;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "*" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * 解析 Authorization 请求头，支持 Bearer 前缀
     */
    private String extractBearerToken(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            return null;
        }
        String value = authorization.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return value.substring(7).trim();
        }
        return value;
    }

    /**
     * 根据 JWT 中的 username 字段反查用户（与签发 Token 时的 resolveJwtUsername 对应）
     */
    private FdUser resolveUserFromJwtUsername(String username) {
        FdUser user = getUserByAccount(username);
        if (user != null) {
            return user;
        }
        try {
            return getUserById(Long.parseLong(username));
        } catch (NumberFormatException ignored) {
            return null;
        }
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
     * 生成访问令牌（与 JeecgBoot 系统登录保持一致：username + password 作为 JWT 签名密钥）
     */
    private String generateToken(FdUser user) {
        return JwtUtil.sign(
                resolveJwtUsername(user),
                resolveJwtSecret(user),
                CommonConstant.CLIENT_TYPE_APP
        );
    }

    /**
     * 生成刷新令牌（有效期为 APP Token 的两倍）
     */
    private String generateRefreshToken(FdUser user) {
        return JwtUtil.sign(
                resolveJwtUsername(user),
                resolveJwtSecret(user),
                JwtUtil.APP_EXPIRE_TIME * 2
        );
    }

    private String resolveJwtUsername(FdUser user) {
        if (oConvertUtils.isNotEmpty(user.getUsername())) {
            return user.getUsername();
        }
        if (oConvertUtils.isNotEmpty(user.getEmail())) {
            return user.getEmail();
        }
        if (oConvertUtils.isNotEmpty(user.getPhone())) {
            return user.getPhone();
        }
        return String.valueOf(user.getId());
    }

    /**
     * JWT HMAC 密钥不能为空，JeecgBoot 约定使用库中存储的加密密码
     */
    private String resolveJwtSecret(FdUser user) {
        if (oConvertUtils.isNotEmpty(user.getPassword())) {
            return user.getPassword();
        }
        throw new JeecgBootException("用户凭证异常，无法生成 Token");
    }

    /**
     * 转换为用户信息VO
     */
    private UserInfoVO convertToUserInfoVO(FdUser user) {
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        boolean protectedAccount = oConvertUtils.isNotEmpty(user.getPhone())
                || oConvertUtils.isNotEmpty(user.getEmail());
        userInfoVO.setAccountProtected(protectedAccount);
        if (userInfoVO.getGrowthValue() == null) {
            userInfoVO.setGrowthValue(0);
        }
        if (userInfoVO.getPoints() == null) {
            userInfoVO.setPoints(0);
        }
        if (userInfoVO.getCheckInDays() == null) {
            userInfoVO.setCheckInDays(0);
        }
        if (userInfoVO.getNotifyEnabled() == null) {
            userInfoVO.setNotifyEnabled(1);
        }
        if (userInfoVO.getActivityEnabled() == null) {
            userInfoVO.setActivityEnabled(1);
        }
        if (userInfoVO.getCookingLevel() == null) {
            userInfoVO.setCookingLevel(1);
        }
        return userInfoVO;
    }
}
