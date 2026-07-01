package org.jeecg.modules.cuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.cuisine.entity.FdRecipeIngredient;
import org.jeecg.modules.cuisine.mapper.FdRecipeIngredientMapper;
import org.jeecg.modules.cuisine.service.IFdRecipeIngredientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FdRecipeIngredientServiceImpl extends ServiceImpl<FdRecipeIngredientMapper, FdRecipeIngredient>
        implements IFdRecipeIngredientService {

    @Override
    public List<FdRecipeIngredient> listByRecipeId(Long recipeId) {
        LambdaQueryWrapper<FdRecipeIngredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeIngredient::getRecipeId, recipeId)
                .orderByAsc(FdRecipeIngredient::getSortNo);
        return list(wrapper);
    }
}
