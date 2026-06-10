package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.dto.EmailLoginDTO;
import org.jeecg.modules.user.dto.PasswordLoginDTO;
import org.jeecg.modules.user.dto.SendEmailCodeDTO;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.service.IVerificationCodeService;
import org.jeecg.modules.user.vo.LoginResultVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 用户登录控制器
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "用户登录")
@RestController
@RequestMapping("/user/auth")
public class LoginController {

    @Resource
    private IFdUserService userService;

    @Resource
    private IVerificationCodeService verificationCodeService;

    /**
     * 发送邮箱验证码
     */
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

    /**
     * 邮箱验证码登录
     */
    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/emailLogin")
    public Result<LoginResultVO> emailLogin(@Validated @RequestBody EmailLoginDTO loginDTO) {
        log.info("邮箱验证码登录请求, email: {}", loginDTO.getEmail());
        
        try {
            LoginResultVO result = userService.emailLogin(loginDTO);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("邮箱验证码登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 账号密码登录
     * 支持用户名、手机号、邮箱作为账号
     */
    @Operation(summary = "账号密码登录")
    @PostMapping("/passwordLogin")
    public Result<LoginResultVO> passwordLogin(@Validated @RequestBody PasswordLoginDTO loginDTO) {
        log.info("账号密码登录请求, account: {}", loginDTO.getAccount());
        
        try {
            LoginResultVO result = userService.passwordLogin(loginDTO);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("账号密码登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 第三方登录
     * 支持 APPLE、GOOGLE、ALIPAY、WECHAT 等登录方式
     */
    @Operation(summary = "第三方登录")
    @PostMapping("/thirdPartyLogin")
    public Result<LoginResultVO> thirdPartyLogin(@Validated @RequestBody ThirdPartyLoginDTO loginDTO) {
        log.info("第三方登录请求, identityType: {}, identifier: {}", 
                loginDTO.getIdentityType(), loginDTO.getIdentifier());
        
        try {
            LoginResultVO result = userService.thirdPartyLogin(loginDTO);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("第三方登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestHeader("Authorization") String token) {
        // TODO: 实现从token中获取用户ID并查询用户信息
        return Result.ok();
    }

    /**
     * 绑定新的第三方账号
     */
    @Operation(summary = "绑定新的第三方账号")
    @PostMapping("/bind")
    public Result<?> bindThirdPartyAccount(@Validated @RequestBody ThirdPartyLoginDTO loginDTO) {
        // TODO: 实现绑定新的第三方账号功能
        return Result.ok();
    }

    /**
     * 解绑第三方账号
     */
    @Operation(summary = "解绑第三方账号")
    @DeleteMapping("/unbind/{identityType}")
    public Result<?> unbindThirdPartyAccount(@PathVariable String identityType) {
        // TODO: 实现解绑第三方账号功能
        return Result.ok();
    }
}
