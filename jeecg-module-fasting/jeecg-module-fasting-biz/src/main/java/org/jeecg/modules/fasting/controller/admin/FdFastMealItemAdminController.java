package org.jeecg.modules.fasting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fasting.entity.FdFastMealItem;
import org.jeecg.modules.fasting.service.IFdFastMealItemService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

@Tag(name = "减脂餐餐次管理")
@RestController
@RequestMapping("/sys/fasting/meal/item")
public class FdFastMealItemAdminController extends JeecgController<FdFastMealItem, IFdFastMealItemService> {

    @GetMapping("/list")
    public Result<IPage<FdFastMealItem>> list(FdFastMealItem item,
                                               @RequestParam(defaultValue = "1") Integer pageNo,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        QueryWrapper<FdFastMealItem> qw = QueryGenerator.initQueryWrapper(item, req.getParameterMap());
        qw.orderByAsc("day_id").orderByAsc("sort_order");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), qw));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody FdFastMealItem item) {
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        service.save(item);
        return Result.OK("添加成功");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdFastMealItem item) {
        item.setUpdateTime(new Date());
        service.updateById(item);
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
