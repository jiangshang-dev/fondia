package org.jeecg.modules.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.dto.RecipeSearchSyncMessage;
import org.jeecg.modules.vo.RecipeSearchItemVO;

public interface IRecipeSearchService {

    void initIndex();

    void syncFromMessage(String messageJson);

    void syncRecipe(RecipeSearchSyncMessage message);

    void removeRecipe(String recipeId);

    IPage<RecipeSearchItemVO> searchRecipes(String keyword, Integer pageNo, Integer pageSize);
}
