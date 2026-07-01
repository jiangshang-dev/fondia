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
import org.jeecg.modules.cuisine.entity.FdRecipe;
import org.jeecg.modules.cuisine.service.IFdRecipeService;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

/**
 * 菜谱管理端接口（与 C 端 /cuisine/recipe 分离）
 */
@Slf4j
@Tag(name = "菜谱管理")
@RestController
@RequestMapping("/sys/fd/recipe")
public class FdRecipeAdminController extends JeecgController<FdRecipe, IFdRecipeService> {

    @Operation(summary = "菜谱分页列表")
    @GetMapping("/list")
    public Result<IPage<FdRecipe>> list(FdRecipe recipe,
                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                         HttpServletRequest req) {
        QueryWrapper<FdRecipe> queryWrapper = QueryGenerator.initQueryWrapper(recipe, req.getParameterMap());
        queryWrapper.orderByDesc("sort_no").orderByDesc("create_time");
        Page<FdRecipe> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, queryWrapper));
    }

    @Operation(summary = "菜谱详情（含步骤/食材/标签）")
    @GetMapping("/queryById")
    public Result<RecipeDetailVO> queryById(@RequestParam Long id) {
        return Result.OK(service.getRecipeDetail(id));
    }

    @AutoLog(value = "新增菜谱", operateType = CommonConstant.OPERATE_TYPE_2)
    @Operation(summary = "新增菜谱")
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdRecipe recipe) {
        fillDefault(recipe, true);
        service.save(recipe);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑菜谱", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "编辑菜谱")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdRecipe recipe) {
        fillDefault(recipe, false);
        service.updateById(recipe);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "更新菜谱状态")
    @Operation(summary = "更新菜谱上下架状态")
    @PutMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        FdRecipe recipe = service.getById(id);
        if (recipe == null) {
            return Result.error("菜谱不存在");
        }
        recipe.setStatus(status);
        recipe.setUpdateTime(new Date());
        if (status != null && status == 1 && recipe.getPublishTime() == null) {
            recipe.setPublishTime(new Date());
        }
        service.updateById(recipe);
        return Result.OK("状态更新成功");
    }

    @AutoLog(value = "删除菜谱", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除菜谱")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除菜谱", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除菜谱")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }

    private void fillDefault(FdRecipe recipe, boolean isCreate) {
        Date now = new Date();
        if (isCreate && recipe.getCreateTime() == null) {
            recipe.setCreateTime(now);
        }
        recipe.setUpdateTime(now);
        if (recipe.getStatus() == null) {
            recipe.setStatus(1);
        }
        if (recipe.getSortNo() == null) {
            recipe.setSortNo(0);
        }
        if (recipe.getLikeCount() == null) {
            recipe.setLikeCount(0);
        }
        if (recipe.getCollectCount() == null) {
            recipe.setCollectCount(0);
        }
        if (recipe.getCommentCount() == null) {
            recipe.setCommentCount(0);
        }
        if (recipe.getViewCount() == null) {
            recipe.setViewCount(0);
        }
        if (recipe.getIsRecommend() == null) {
            recipe.setIsRecommend(0);
        }
        if (isCreate && recipe.getStatus() == 1 && recipe.getPublishTime() == null) {
            recipe.setPublishTime(now);
        }
    }
}
