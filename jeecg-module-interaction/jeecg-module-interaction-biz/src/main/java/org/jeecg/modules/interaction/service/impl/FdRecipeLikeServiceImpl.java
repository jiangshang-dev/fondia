package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.interaction.entity.FdRecipeLike;
import org.jeecg.modules.interaction.mapper.FdRecipeLikeMapper;
import org.jeecg.modules.interaction.service.IFdRecipeLikeService;
import org.jeecg.modules.interaction.vo.ToggleResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class FdRecipeLikeServiceImpl extends ServiceImpl<FdRecipeLikeMapper, FdRecipeLike>
        implements IFdRecipeLikeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ToggleResultVO toggleLike(Long recipeId, Long userId) {
        LambdaQueryWrapper<FdRecipeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeLike::getRecipeId, recipeId)
                .eq(FdRecipeLike::getUserId, userId);
        FdRecipeLike existing = getOne(wrapper, false);
        boolean active;
        if (existing != null) {
            removeById(existing.getId());
            active = false;
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
        return ToggleResultVO.builder().active(active).count(count).build();
    }

    @Override
    public boolean isLiked(Long recipeId, Long userId) {
        LambdaQueryWrapper<FdRecipeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeLike::getRecipeId, recipeId)
                .eq(FdRecipeLike::getUserId, userId);
        return count(wrapper) > 0;
    }
}
