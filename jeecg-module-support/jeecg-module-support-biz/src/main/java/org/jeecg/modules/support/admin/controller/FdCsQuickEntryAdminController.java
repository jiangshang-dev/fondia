package org.jeecg.modules.support.admin.controller;

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
import org.jeecg.modules.support.entity.FdCsQuickEntry;
import org.jeecg.modules.support.service.IFdCsQuickEntryService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@Tag(name = "客服快捷入口管理")
@RestController
@RequestMapping("/sys/fd/cs/quickEntry")
public class FdCsQuickEntryAdminController extends JeecgController<FdCsQuickEntry, IFdCsQuickEntryService> {

    @Operation(summary = "快捷入口分页列表")
    @GetMapping("/list")
    public Result<IPage<FdCsQuickEntry>> list(FdCsQuickEntry entry,
                                              @RequestParam(defaultValue = "1") Integer pageNo,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              HttpServletRequest req) {
        QueryWrapper<FdCsQuickEntry> queryWrapper = QueryGenerator.initQueryWrapper(entry, req.getParameterMap());
        queryWrapper.orderByAsc("sort_no").orderByDesc("create_time");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), queryWrapper));
    }

    @AutoLog(value = "新增快捷入口", operateType = CommonConstant.OPERATE_TYPE_2)
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdCsQuickEntry entry) {
        long now = System.currentTimeMillis();
        entry.setCreateTime(now);
        entry.setUpdateTime(now);
        if (entry.getStatus() == null) {
            entry.setStatus(1);
        }
        service.save(entry);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑快捷入口", operateType = CommonConstant.OPERATE_TYPE_3)
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdCsQuickEntry entry) {
        entry.setUpdateTime(System.currentTimeMillis());
        service.updateById(entry);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除快捷入口", operateType = CommonConstant.OPERATE_TYPE_4)
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
