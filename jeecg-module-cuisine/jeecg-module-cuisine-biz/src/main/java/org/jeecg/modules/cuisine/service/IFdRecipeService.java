package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdRecipe;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeListItemVO;

import java.util.List;

public interface IFdRecipeService extends IService<FdRecipe> {

    IPage<RecipeListItemVO> pageRecipes(Long cuisineId, Long categoryId, Integer subType,
                                        Long tagId, String keyword,
                                        Integer pageNo, Integer pageSize);

    List<RecipeListItemVO> listByCategoryId(Long categoryId, Integer limit);

    RecipeDetailVO getRecipeDetail(Long recipeId);

    void incrementViewCount(Long recipeId);
}
