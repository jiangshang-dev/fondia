package org.jeecg.modules.user.admin.controller;

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
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * App用户管理端接口（与 C 端 /user/auth、/user/profile 分离）
 */
@Slf4j
@Tag(name = "App用户管理")
@RestController
@RequestMapping("/sys/fd/user")
public class FdUserAdminController extends JeecgController<FdUser, IFdUserService> {

    @Operation(summary = "App用户分页列表")
    @GetMapping("/list")
    public Result<IPage<FdUser>> list(FdUser fdUser,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<FdUser> queryWrapper = QueryGenerator.initQueryWrapper(fdUser, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<FdUser> page = new Page<>(pageNo, pageSize);
        IPage<FdUser> pageList = service.page(page, queryWrapper);
        pageList.getRecords().forEach(this::maskPassword);
        return Result.OK(pageList);
    }

    @Operation(summary = "App用户详情")
    @GetMapping("/queryById")
    public Result<FdUser> queryById(@RequestParam(name = "id") Long id) {
        FdUser user = service.getById(id);
        maskPassword(user);
        return Result.OK(user);
    }

    @AutoLog(value = "编辑App用户", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "编辑App用户")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdUser fdUser) {
        fdUser.setPassword(null);
        fdUser.setUpdateTime(System.currentTimeMillis());
        service.updateById(fdUser);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "更新App用户状态")
    @Operation(summary = "更新App用户状态")
    @PutMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        FdUser user = service.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(System.currentTimeMillis());
        service.updateById(user);
        return Result.OK("状态更新成功");
    }

    @AutoLog(value = "删除App用户", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除App用户")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam(name = "id") Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除App用户", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除App用户")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }

    private void maskPassword(FdUser user) {
        if (user != null) {
            user.setPassword(null);
        }
    }
}
