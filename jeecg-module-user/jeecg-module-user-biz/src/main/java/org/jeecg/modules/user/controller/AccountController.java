package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.dto.DeleteAccountDTO;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.AccountDeletePreviewVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "账号管理")
@RestController
@RequestMapping("/user/account")
public class AccountController {

    @Resource
    private IFdUserService userService;

    @Operation(summary = "注销前验证信息预览")
    @GetMapping("/deletePreview")
    public Result<AccountDeletePreviewVO> deletePreview(@RequestHeader("Authorization") String authorization) {
        return Result.ok(userService.getAccountDeletePreview(authorization));
    }

    @Operation(summary = "发送注销验证码")
    @PostMapping("/sendDeleteCode")
    public Result<String> sendDeleteCode(@RequestHeader("Authorization") String authorization) {
        userService.sendAccountDeleteCode(authorization);
        return Result.ok("验证码发送成功");
    }

    @Operation(summary = "申请注销账号")
    @PostMapping("/delete")
    public Result<String> deleteAccount(@RequestHeader("Authorization") String authorization,
                                        @Validated @RequestBody DeleteAccountDTO dto) {
        userService.deleteAccount(authorization, dto.getCode());
        return Result.ok("账号已注销");
    }
}
