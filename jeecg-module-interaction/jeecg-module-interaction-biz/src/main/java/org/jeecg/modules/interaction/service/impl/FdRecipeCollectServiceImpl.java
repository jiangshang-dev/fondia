package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.interaction.entity.FdRecipeCollect;
import org.jeecg.modules.interaction.mapper.FdRecipeCollectMapper;
import org.jeecg.modules.interaction.mapper.RecipeStatMapper;
import org.jeecg.modules.interaction.service.IFdRecipeCollectService;
import org.jeecg.modules.interaction.vo.ToggleResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;

@Service
public class FdRecipeCollectServiceImpl extends ServiceImpl<FdRecipeCollectMapper, FdRecipeCollect>
        implements IFdRecipeCollectService {

    @Resource
    private RecipeStatMapper recipeStatMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ToggleResultVO toggleCollect(Long recipeId, Long userId) {
        FdRecipeCollect existing = baseMapper.selectOneIgnoreLogic(userId, recipeId);
        boolean active;
        if (isActive(existing)) {
            baseMapper.physicalDeleteByUserRecipe(userId, recipeId);
            active = false;
        } else if (existing != null) {
            baseMapper.restoreById(existing.getId());
            active = true;
        } else {
            Date now = new Date();
            FdRecipeCollect collect = new FdRecipeCollect();
            collect.setRecipeId(recipeId);
            collect.setUserId(userId);
            collect.setCreateTime(now);
            collect.setUpdateTime(now);
            save(collect);
            active = true;
        }
        long count = count(new LambdaQueryWrapper<FdRecipeCollect>().eq(FdRecipeCollect::getRecipeId, recipeId));
        recipeStatMapper.updateCollectCount(recipeId, (int) count);
        return ToggleResultVO.builder().active(active).count(count).build();
    }

    @Override
    public boolean isCollected(Long recipeId, Long userId) {
        LambdaQueryWrapper<FdRecipeCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCollect::getRecipeId, recipeId)
                .eq(FdRecipeCollect::getUserId, userId);
        return count(wrapper) > 0;
    }

    @Override
    public IPage<FdRecipeCollect> pageMyCollects(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdRecipeCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCollect::getUserId, userId)
                .orderByDesc(FdRecipeCollect::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    private boolean isActive(FdRecipeCollect entity) {
        return entity != null && (entity.getDelFlag() == null || entity.getDelFlag() == 0);
    }
}
