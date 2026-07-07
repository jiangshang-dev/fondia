package org.jeecg.modules.fasting.controller.admin;

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
import org.jeecg.modules.fasting.entity.FdFastPlan;
import org.jeecg.modules.fasting.service.IFdFastPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

@Slf4j
@Tag(name = "断食计划管理")
@RestController
@RequestMapping("/sys/fasting/plan")
public class FdFastPlanAdminController extends JeecgController<FdFastPlan, IFdFastPlanService> {

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<IPage<FdFastPlan>> list(FdFastPlan plan,
                                          @RequestParam(defaultValue = "1") Integer pageNo,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        QueryWrapper<FdFastPlan> qw = QueryGenerator.initQueryWrapper(plan, req.getParameterMap());
        qw.orderByAsc("sort_order");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), qw));
    }

    @AutoLog(value = "新增断食计划", operateType = CommonConstant.OPERATE_TYPE_2)
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdFastPlan plan) {
        plan.setCreateTime(new Date());
        plan.setUpdateTime(new Date());
        service.save(plan);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑断食计划", operateType = CommonConstant.OPERATE_TYPE_3)
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdFastPlan plan) {
        plan.setUpdateTime(new Date());
        service.updateById(plan);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除断食计划", operateType = CommonConstant.OPERATE_TYPE_4)
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除断食计划", operateType = CommonConstant.OPERATE_TYPE_4)
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
