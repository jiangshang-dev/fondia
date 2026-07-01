package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.interaction.entity.FdRecipeLike;
import org.jeecg.modules.interaction.mapper.FdRecipeLikeMapper;
import org.jeecg.modules.interaction.mapper.RecipeStatMapper;
import org.jeecg.modules.interaction.service.IFdRecipeLikeService;
import org.jeecg.modules.interaction.vo.ToggleResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;

@Service
public class FdRecipeLikeServiceImpl extends ServiceImpl<FdRecipeLikeMapper, FdRecipeLike>
        implements IFdRecipeLikeService {

    @Resource
    private RecipeStatMapper recipeStatMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ToggleResultVO toggleLike(Long recipeId, Long userId) {
        FdRecipeLike existing = baseMapper.selectOneIgnoreLogic(userId, recipeId);
        boolean active;
        if (isActive(existing)) {
            baseMapper.physicalDeleteByUserRecipe(userId, recipeId);
            active = false;
        } else if (existing != null) {
            baseMapper.restoreById(existing.getId());
            active = true;
        } else {
            Date now = new Date();
            FdRecipeLike like = new FdRecipeLike();
            like.setRecipeId(recipeId);
            like.setUserId(userId);
            like.setCreateTime(now);
            like.setUpdateTime(now);
            save(like);
            active = true;
        }
        long count = count(new LambdaQueryWrapper<FdRecipeLike>().eq(FdRecipeLike::getRecipeId, recipeId));
        recipeStatMapper.updateLikeCount(recipeId, (int) count);
        return ToggleResultVO.builder().active(active).count(count).build();
    }

    @Override
    public boolean isLiked(Long recipeId, Long userId) {
        LambdaQueryWrapper<FdRecipeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeLike::getRecipeId, recipeId)
                .eq(FdRecipeLike::getUserId, userId);
        return count(wrapper) > 0;
    }

    @Override
    public IPage<FdRecipeLike> pageMyLikes(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdRecipeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeLike::getUserId, userId)
                .orderByDesc(FdRecipeLike::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    private boolean isActive(FdRecipeLike entity) {
        return entity != null && (entity.getDelFlag() == null || entity.getDelFlag() == 0);
    }
}
