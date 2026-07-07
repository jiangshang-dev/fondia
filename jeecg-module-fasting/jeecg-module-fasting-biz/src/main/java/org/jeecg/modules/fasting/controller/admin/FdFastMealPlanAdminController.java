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
import org.jeecg.modules.fasting.entity.FdFastMealPlan;
import org.jeecg.modules.fasting.service.IFdFastMealPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Slf4j
@Tag(name = "减脂餐方案管理")
@RestController
@RequestMapping("/sys/fasting/meal/plan")
public class FdFastMealPlanAdminController extends JeecgController<FdFastMealPlan, IFdFastMealPlanService> {

    @GetMapping("/list")
    public Result<IPage<FdFastMealPlan>> list(FdFastMealPlan plan,
                                             @RequestParam(defaultValue = "1") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             HttpServletRequest req) {
        QueryWrapper<FdFastMealPlan> qw = QueryGenerator.initQueryWrapper(plan, req.getParameterMap());
        qw.orderByAsc("sort_order").orderByDesc("create_time");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), qw));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody FdFastMealPlan plan) {
        plan.setCreateTime(new Date());
        plan.setUpdateTime(new Date());
        if (plan.getIsDefault() == null) {
            plan.setIsDefault(0);
        }
        service.save(plan);
        return Result.OK("添加成功");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdFastMealPlan plan) {
        plan.setUpdateTime(new Date());
        service.updateById(plan);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "设为默认减脂餐方案")
    @PutMapping("/setDefault")
    public Result<String> setDefault(@RequestBody Map<String, Long> body) {
        service.setDefaultPlan(body.get("id"));
        return Result.OK("已设为默认方案");
    }

    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
