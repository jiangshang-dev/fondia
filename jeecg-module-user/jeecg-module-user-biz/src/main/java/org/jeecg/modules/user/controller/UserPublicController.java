package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserBriefVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户公开信息")
@RestController
@RequestMapping("/user/public")
public class UserPublicController {

    @Resource
    private IFdUserService userService;

    @Operation(summary = "用户简要信息")
    @GetMapping("/brief/{userId}")
    public Result<UserBriefVO> brief(@PathVariable Long userId) {
        FdUser user = userService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.ok(UserBriefVO.builder()
                .id(user.getId())
                .nickname(resolveNickname(user))
                .avatar(user.getAvatar())
                .build());
    }

    private String resolveNickname(FdUser user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return "用户" + user.getId();
    }
}
