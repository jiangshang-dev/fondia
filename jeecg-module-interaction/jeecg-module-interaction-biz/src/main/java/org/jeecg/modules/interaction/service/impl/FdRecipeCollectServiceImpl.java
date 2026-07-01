package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.interaction.entity.FdRecipeCollect;
import org.jeecg.modules.interaction.mapper.FdRecipeCollectMapper;
import org.jeecg.modules.interaction.service.IFdRecipeCollectService;
import org.jeecg.modules.interaction.vo.ToggleResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class FdRecipeCollectServiceImpl extends ServiceImpl<FdRecipeCollectMapper, FdRecipeCollect>
        implements IFdRecipeCollectService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ToggleResultVO toggleCollect(Long recipeId, Long userId) {
        LambdaQueryWrapper<FdRecipeCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCollect::getRecipeId, recipeId)
                .eq(FdRecipeCollect::getUserId, userId);
        FdRecipeCollect existing = getOne(wrapper, false);
        boolean active;
        if (existing != null) {
            removeById(existing.getId());
            active = false;
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
        return ToggleResultVO.builder().active(active).count(count).build();
    }

    @Override
    public IPage<FdRecipeCollect> pageMyCollects(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdRecipeCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCollect::getUserId, userId)
                .orderByDesc(FdRecipeCollect::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }
}
