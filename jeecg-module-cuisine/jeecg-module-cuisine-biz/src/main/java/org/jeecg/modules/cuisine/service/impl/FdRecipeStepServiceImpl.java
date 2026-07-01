package org.jeecg.modules.cuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.cuisine.entity.FdRecipeStep;
import org.jeecg.modules.cuisine.mapper.FdRecipeStepMapper;
import org.jeecg.modules.cuisine.service.IFdRecipeStepService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FdRecipeStepServiceImpl extends ServiceImpl<FdRecipeStepMapper, FdRecipeStep>
        implements IFdRecipeStepService {

    @Override
    public List<FdRecipeStep> listByRecipeId(Long recipeId) {
        LambdaQueryWrapper<FdRecipeStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeStep::getRecipeId, recipeId)
                .orderByAsc(FdRecipeStep::getStepNo)
                .orderByAsc(FdRecipeStep::getSortNo);
        return list(wrapper);
    }
}
