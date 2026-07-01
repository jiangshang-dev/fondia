package org.jeecg.modules.cuisine.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.cuisine.service.IFdRecipeService;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeListItemVO;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "菜谱")
@RestController
@RequestMapping("/cuisine/recipe")
public class RecipeController {

    @Resource
    private IFdRecipeService recipeService;

    @Operation(summary = "菜谱分页列表")
    @GetMapping("/list")
    public Result<IPage<RecipeListItemVO>> list(@RequestParam(required = false) Long cuisineId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) Integer subType,
                                                @RequestParam(required = false) Long tagId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(recipeService.pageRecipes(cuisineId, categoryId, subType, tagId, keyword, pageNo, pageSize));
    }

    @Operation(summary = "菜谱详情")
    @GetMapping("/{recipeId}")
    public Result<RecipeDetailVO> detail(@PathVariable Long recipeId) {
        return Result.ok(recipeService.getRecipeDetail(recipeId));
    }

    @Operation(summary = "增加浏览量")
    @PostMapping("/{recipeId}/view")
    public Result<?> incrementView(@PathVariable Long recipeId) {
        recipeService.incrementViewCount(recipeId);
        return Result.ok("ok");
    }
}
