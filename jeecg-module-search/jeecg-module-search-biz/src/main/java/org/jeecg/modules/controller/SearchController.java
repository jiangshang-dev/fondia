package org.jeecg.modules.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.service.IRecipeSearchService;
import org.jeecg.modules.vo.RecipeSearchItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "搜索(App)")
@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private IRecipeSearchService recipeSearchService;

    @IgnoreAuth
    @Operation(summary = "搜索菜谱")
    @GetMapping("/recipe")
    public Result<IPage<RecipeSearchItemVO>> searchRecipe(@RequestParam String keyword,
                                                          @RequestParam(defaultValue = "1") Integer pageNo,
                                                          @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.OK(recipeSearchService.searchRecipes(keyword, pageNo, pageSize));
    }
}
