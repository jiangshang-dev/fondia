package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdRecipe;
import org.jeecg.modules.cuisine.vo.RecipeAdminDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeAdminSaveDTO;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeListItemVO;

import java.util.List;

public interface IFdRecipeService extends IService<FdRecipe> {

    IPage<RecipeListItemVO> pageRecipes(Long cuisineId, Long categoryId, Integer subType,
                                        Long tagId, String keyword,
                                        Integer pageNo, Integer pageSize);

    List<RecipeListItemVO> listByCategoryId(Long categoryId, Integer limit);

    RecipeDetailVO getRecipeDetail(Long recipeId);

    RecipeAdminDetailVO getRecipeAdminDetail(Long recipeId);

    Long saveRecipeAdmin(RecipeAdminSaveDTO dto, boolean isUpdate);

    void incrementViewCount(Long recipeId);

    /** 发布菜谱搜索索引同步消息 */
    void publishRecipeSearchSync(Long recipeId);

    /** 从搜索索引删除菜谱 */
    void removeRecipeFromSearch(Long recipeId);

    /** 全量重建搜索索引（MySQL 已有数据但 ES 为空时调用） */
    int rebuildAllRecipeSearchIndex();
}
