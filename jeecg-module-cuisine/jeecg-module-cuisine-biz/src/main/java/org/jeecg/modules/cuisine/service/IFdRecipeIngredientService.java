package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdRecipeIngredient;

import java.util.List;

public interface IFdRecipeIngredientService extends IService<FdRecipeIngredient> {

    List<FdRecipeIngredient> listByRecipeId(Long recipeId);
}
