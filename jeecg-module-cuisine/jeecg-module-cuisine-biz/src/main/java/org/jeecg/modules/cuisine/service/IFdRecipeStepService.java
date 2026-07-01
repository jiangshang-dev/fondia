package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdRecipeStep;

import java.util.List;

public interface IFdRecipeStepService extends IService<FdRecipeStep> {

    List<FdRecipeStep> listByRecipeId(Long recipeId);
}
