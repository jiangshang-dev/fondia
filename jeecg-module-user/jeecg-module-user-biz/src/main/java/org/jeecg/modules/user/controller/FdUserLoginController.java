package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.dto.EmailLoginDTO;
import org.jeecg.modules.user.dto.HuaweiLoginDTO;
import org.jeecg.modules.user.dto.PasswordLoginDTO;
import org.jeecg.modules.user.dto.ResetPasswordDTO;
import org.jeecg.modules.user.dto.SendEmailCodeDTO;
import org.jeecg.modules.user.dto.SendResetPasswordCodeDTO;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.dto.WxMiniLoginDTO;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.service.IVerificationCodeService;
import org.jeecg.modules.user.vo.LoginResultVO;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * App/小程序用户登录（与 system 模块 LoginController 分离，避免单体 Bean 名冲突）
 */
@Slf4j
@Tag(name = "App用户登录")
@RestController
@RequestMapping("/user/auth")
public class FdUserLoginController {

    @Resource
    private IFdUserService userService;

    @Resource
    private IVerificationCodeService verificationCodeService;

    @GetMapping("/test")
    public Result<?> test() {
        return Result.ok("test");
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/sendEmailCode")
    public Result<?> sendEmailCode(@Validated @RequestBody SendEmailCodeDTO dto) {
        log.info("发送邮箱验证码请求, email: {}, scene: {}", dto.getEmail(), dto.getScene());
        try {
            verificationCodeService.sendEmailCode(dto.getEmail(), dto.getScene());
            return Result.ok("验证码发送成功");
        } catch (Exception e) {
            log.error("发送邮箱验证码失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/emailLogin")
    public Result<LoginResultVO> emailLogin(@Validated @RequestBody EmailLoginDTO loginDTO) {
        log.info("邮箱验证码登录请求, email: {}", loginDTO.getEmail());
        try {
            return Result.ok(userService.emailLogin(loginDTO));
        } catch (Exception e) {
            log.error("邮箱验证码登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "账号密码登录")
    @PostMapping({"/passwordLogin", "/login"})
    public Result<LoginResultVO> passwordLogin(@Validated @RequestBody PasswordLoginDTO loginDTO) {
        log.info("账号密码登录请求, account: {}", loginDTO.getAccount());
        try {
            return Result.ok(userService.passwordLogin(loginDTO));
        } catch (Exception e) {
            log.error("账号密码登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "发送重置密码验证码")
    @PostMapping("/sendResetPasswordCode")
    public Result<String> sendResetPasswordCode(@Validated @RequestBody SendResetPasswordCodeDTO dto) {
        try {
            userService.sendResetPasswordCode(dto);
            return Result.ok("验证码发送成功");
        } catch (Exception e) {
            log.error("发送重置密码验证码失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        try {
            userService.resetPassword(dto);
            return Result.ok("密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "华为账号一键登录")
    @PostMapping("/huaweiLogin")
    public Result<LoginResultVO> huaweiLogin(@Validated @RequestBody HuaweiLoginDTO loginDTO) {
        log.info("华为账号登录请求");
        try {
            return Result.ok(userService.huaweiLogin(loginDTO));
        } catch (Exception e) {
            log.error("华为账号登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "微信小程序登录")
    @PostMapping("/wxMiniLogin")
    public Result<LoginResultVO> wxMiniLogin(@Validated @RequestBody WxMiniLoginDTO loginDTO) {
        try {
            return Result.ok(userService.wxMiniLogin(loginDTO));
        } catch (Exception e) {
            log.error("微信小程序登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "第三方登录")
    @PostMapping("/thirdPartyLogin")
    public Result<LoginResultVO> thirdPartyLogin(@Validated @RequestBody ThirdPartyLoginDTO loginDTO) {
        log.info("第三方登录请求, identityType: {}, identifier: {}",
                loginDTO.getIdentityType(), loginDTO.getIdentifier());
        try {
            return Result.ok(userService.thirdPartyLogin(loginDTO));
        } catch (Exception e) {
            log.error("第三方登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestHeader("Authorization") String authorization) {
        return Result.ok(userService.getUserInfo(authorization));
    }

    @Operation(summary = "绑定新的第三方账号")
    @PostMapping("/bind")
    public Result<?> bindThirdPartyAccount(@Validated @RequestBody ThirdPartyLoginDTO loginDTO) {
        return Result.ok();
    }

    @Operation(summary = "解绑第三方账号")
    @DeleteMapping("/unbind/{identityType}")
    public Result<?> unbindThirdPartyAccount(@PathVariable String identityType) {
        return Result.ok();
    }
}
