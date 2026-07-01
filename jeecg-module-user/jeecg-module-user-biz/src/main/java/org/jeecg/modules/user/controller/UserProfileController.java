package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.dto.UpdateUserProfileDTO;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户资料")
@RestController
@RequestMapping("/user/profile")
public class UserProfileController {

    @Resource
    private IFdUserService userService;

    @Operation(summary = "获取当前用户资料")
    @GetMapping
    public Result<UserInfoVO> getProfile(@RequestHeader("Authorization") String authorization) {
        return Result.ok(userService.getUserInfo(authorization));
    }

    @Operation(summary = "更新用户资料")
    @PutMapping
    public Result<UserInfoVO> updateProfile(@RequestHeader("Authorization") String authorization,
                                            @RequestBody UpdateUserProfileDTO dto) {
        return Result.ok(userService.updateUserProfile(authorization, dto));
    }
}
