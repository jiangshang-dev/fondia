package org.jeecg.modules.user.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.modules.user.entity.FdUserFeedback;
import org.jeecg.modules.user.service.IFdUserFeedbackService;
import org.jeecg.modules.user.vo.FeedbackAdminVO;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@Tag(name = "意见反馈管理")
@RestController
@RequestMapping("/sys/fd/feedback")
public class FdUserFeedbackAdminController {

    @Resource
    private IFdUserFeedbackService feedbackService;

    @Operation(summary = "反馈分页列表")
    @GetMapping("/list")
    public Result<IPage<FeedbackAdminVO>> list(FdUserFeedback feedback,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return Result.OK(feedbackService.pageForAdmin(feedback, pageNo, pageSize));
    }

    @Operation(summary = "反馈详情")
    @GetMapping("/queryById")
    public Result<FeedbackAdminVO> queryById(@RequestParam Long id) {
        return Result.OK(feedbackService.getAdminDetail(id));
    }

    @AutoLog(value = "标记反馈已处理")
    @Operation(summary = "更新反馈状态")
    @PutMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        feedbackService.updateStatus(id, status);
        return Result.OK("状态更新成功");
    }

    @AutoLog(value = "删除意见反馈", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除反馈")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        feedbackService.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除意见反馈", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除反馈")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        feedbackService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
