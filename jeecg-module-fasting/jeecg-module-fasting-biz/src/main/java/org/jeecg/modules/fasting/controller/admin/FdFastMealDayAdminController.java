package org.jeecg.modules.fasting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fasting.entity.FdFastMealDay;
import org.jeecg.modules.fasting.service.IFdFastMealDayService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

@Tag(name = "减脂餐日配置")
@RestController
@RequestMapping("/sys/fasting/meal/day")
public class FdFastMealDayAdminController extends JeecgController<FdFastMealDay, IFdFastMealDayService> {

    @GetMapping("/list")
    public Result<IPage<FdFastMealDay>> list(FdFastMealDay day,
                                             @RequestParam(defaultValue = "1") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             HttpServletRequest req) {
        QueryWrapper<FdFastMealDay> qw = QueryGenerator.initQueryWrapper(day, req.getParameterMap());
        qw.orderByAsc("day_of_week");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), qw));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody FdFastMealDay day) {
        day.setCreateTime(new Date());
        day.setUpdateTime(new Date());
        service.save(day);
        return Result.OK("添加成功");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdFastMealDay day) {
        day.setUpdateTime(new Date());
        service.updateById(day);
        return Result.OK("编辑成功");
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
