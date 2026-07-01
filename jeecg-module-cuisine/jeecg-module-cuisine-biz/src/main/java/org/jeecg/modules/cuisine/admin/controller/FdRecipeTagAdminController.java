package org.jeecg.modules.cuisine.admin.controller;

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
import org.jeecg.modules.cuisine.entity.FdRecipeTag;
import org.jeecg.modules.cuisine.service.IFdRecipeTagService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

/**
 * 菜谱标签管理端接口（与 C 端 /cuisine/tag 分离）
 */
@Slf4j
@Tag(name = "菜谱标签管理")
@RestController
@RequestMapping("/sys/fd/recipeTag")
public class FdRecipeTagAdminController extends JeecgController<FdRecipeTag, IFdRecipeTagService> {

    @Operation(summary = "标签分页列表")
    @GetMapping("/list")
    public Result<IPage<FdRecipeTag>> list(FdRecipeTag tag,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
        QueryWrapper<FdRecipeTag> queryWrapper = QueryGenerator.initQueryWrapper(tag, req.getParameterMap());
        queryWrapper.orderByAsc("sort_no").orderByDesc("create_time");
        Page<FdRecipeTag> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, queryWrapper));
    }

    @Operation(summary = "标签详情")
    @GetMapping("/queryById")
    public Result<FdRecipeTag> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }

    @AutoLog(value = "新增菜谱标签", operateType = CommonConstant.OPERATE_TYPE_2)
    @Operation(summary = "新增标签")
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdRecipeTag tag) {
        fillDefault(tag, true);
        service.save(tag);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑菜谱标签", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "编辑标签")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdRecipeTag tag) {
        fillDefault(tag, false);
        service.updateById(tag);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除菜谱标签", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除标签")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除菜谱标签", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除标签")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }

    private void fillDefault(FdRecipeTag tag, boolean isCreate) {
        Date now = new Date();
        if (isCreate && tag.getCreateTime() == null) {
            tag.setCreateTime(now);
        }
        tag.setUpdateTime(now);
        if (tag.getStatus() == null) {
            tag.setStatus(1);
        }
        if (tag.getSortNo() == null) {
            tag.setSortNo(0);
        }
    }
}
