package org.jeecg.modules.cuisine.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.cuisine.service.IFdRecipeCategoryService;
import org.jeecg.modules.cuisine.vo.CategoryBriefVO;
import org.jeecg.modules.cuisine.vo.CategoryTreeVO;
import org.jeecg.modules.cuisine.vo.CuisineDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "菜谱分类")
@RestController
@RequestMapping("/cuisine/category")
public class RecipeCategoryController {

    @Resource
    private IFdRecipeCategoryService categoryService;

    @Operation(summary = "热门菜系列表")
    @GetMapping("/hot")
    public Result<List<CategoryBriefVO>> hotList() {
        return Result.ok(categoryService.listHotCuisines());
    }

    @Operation(summary = "全部菜系列表")
    @GetMapping("/list")
    public Result<List<CategoryBriefVO>> list() {
        return Result.ok(categoryService.listAllCuisines());
    }

    @Operation(summary = "菜系详情（头部信息+标签筛选项）")
    @GetMapping("/{cuisineId}")
    public Result<CuisineDetailVO> detail(@PathVariable Long cuisineId) {
        return Result.ok(categoryService.getCuisineDetail(cuisineId));
    }

    @Operation(summary = "菜系下子分类（主食/菜）")
    @GetMapping("/{cuisineId}/subs")
    public Result<List<CategoryBriefVO>> subs(@PathVariable Long cuisineId) {
        return Result.ok(categoryService.listSubCategories(cuisineId));
    }

    @Operation(summary = "分类页树形内容（主食/菜分区+菜谱）")
    @GetMapping("/{cuisineId}/tree")
    public Result<CategoryTreeVO> tree(@PathVariable Long cuisineId) {
        return Result.ok(categoryService.getCategoryTree(cuisineId));
    }
}
