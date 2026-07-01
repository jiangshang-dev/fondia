package org.jeecg.modules.interaction.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.interaction.entity.FdRecipeComment;
import org.jeecg.modules.interaction.enums.CommentStatusEnum;
import org.jeecg.modules.interaction.service.IFdRecipeCommentService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

/**
 * 评论审核管理端接口（与 C 端 /interaction/comment 分离）
 */
@Slf4j
@Tag(name = "评论审核管理")
@RestController
@RequestMapping("/sys/fd/comment")
public class FdRecipeCommentAdminController extends JeecgController<FdRecipeComment, IFdRecipeCommentService> {

    @Operation(summary = "评论分页列表")
    @GetMapping("/list")
    public Result<IPage<FdRecipeComment>> list(FdRecipeComment comment,
                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              HttpServletRequest req) {
        QueryWrapper<FdRecipeComment> queryWrapper = QueryGenerator.initQueryWrapper(comment, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<FdRecipeComment> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, queryWrapper));
    }

    @Operation(summary = "评论详情")
    @GetMapping("/queryById")
    public Result<FdRecipeComment> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }

    @AutoLog(value = "审核评论")
    @Operation(summary = "审核评论")
    @PutMapping("/audit")
    public Result<String> audit(@RequestParam Long id, @RequestParam Integer status) {
        FdRecipeComment comment = service.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        CommentStatusEnum statusEnum = CommentStatusEnum.of(status);
        comment.setStatus(statusEnum.getCode());
        comment.setUpdateTime(new Date());
        service.updateById(comment);
        return Result.OK("审核成功");
    }

    @AutoLog(value = "隐藏评论", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "隐藏评论")
    @PutMapping("/hide")
    public Result<String> hide(@RequestParam Long id) {
        return audit(id, CommentStatusEnum.HIDDEN.getCode());
    }

    @AutoLog(value = "通过评论", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "通过评论")
    @PutMapping("/publish")
    public Result<String> publish(@RequestParam Long id) {
        return audit(id, CommentStatusEnum.PUBLISHED.getCode());
    }

    @AutoLog(value = "删除评论", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除评论")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除评论", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除评论")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
