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
import org.jeecg.modules.cuisine.entity.FdRecipeCategory;
import org.jeecg.modules.cuisine.service.IFdRecipeCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 菜系/分类管理端接口（与 C 端 /cuisine/category 分离）
 */
@Slf4j
@Tag(name = "菜系分类管理")
@RestController
@RequestMapping("/sys/fd/recipeCategory")
public class FdRecipeCategoryAdminController extends JeecgController<FdRecipeCategory, IFdRecipeCategoryService> {

    @Operation(summary = "分类分页列表")
    @GetMapping("/list")
    public Result<IPage<FdRecipeCategory>> list(FdRecipeCategory category,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<FdRecipeCategory> queryWrapper = QueryGenerator.initQueryWrapper(category, req.getParameterMap());
        queryWrapper.orderByAsc("sort_no").orderByDesc("create_time");
        Page<FdRecipeCategory> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, queryWrapper));
    }

    @Operation(summary = "分类树形列表")
    @GetMapping("/tree")
    public Result<List<FdRecipeCategory>> tree() {
        QueryWrapper<FdRecipeCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_no");
        return Result.OK(service.list(wrapper));
    }

    @Operation(summary = "分类详情")
    @GetMapping("/queryById")
    public Result<FdRecipeCategory> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }

    @AutoLog(value = "新增菜系分类", operateType = CommonConstant.OPERATE_TYPE_2)
    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdRecipeCategory category) {
        fillDefault(category, true);
        service.save(category);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑菜系分类", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "编辑分类")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdRecipeCategory category) {
        fillDefault(category, false);
        service.updateById(category);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除菜系分类", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除分类")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除菜系分类", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除分类")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }

    private void fillDefault(FdRecipeCategory category, boolean isCreate) {
        Date now = new Date();
        if (isCreate && category.getCreateTime() == null) {
            category.setCreateTime(now);
        }
        category.setUpdateTime(now);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortNo() == null) {
            category.setSortNo(0);
        }
    }
}
