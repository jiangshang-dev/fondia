package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.dto.FeedbackSubmitDTO;
import org.jeecg.modules.user.service.IFdUserFeedbackService;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "意见反馈")
@RestController
@RequestMapping("/user/feedback")
public class FeedbackController {

    @Resource
    private IFdUserService userService;

    @Resource
    private IFdUserFeedbackService feedbackService;

    @Operation(summary = "提交意见反馈")
    @PostMapping("/submit")
    public Result<String> submit(@RequestHeader("Authorization") String authorization,
                                 @Validated @RequestBody FeedbackSubmitDTO dto) {
        feedbackService.submitFeedback(userService.getUserInfo(authorization), dto);
        return Result.ok("提交成功");
    }
}
