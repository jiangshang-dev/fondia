package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.interaction.entity.FdRecipeBrowseHistory;
import org.jeecg.modules.interaction.mapper.FdRecipeBrowseHistoryMapper;
import org.jeecg.modules.interaction.service.IFdRecipeBrowseHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class FdRecipeBrowseHistoryServiceImpl extends ServiceImpl<FdRecipeBrowseHistoryMapper, FdRecipeBrowseHistory>
        implements IFdRecipeBrowseHistoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBrowse(Long recipeId, Long userId) {
        LambdaQueryWrapper<FdRecipeBrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeBrowseHistory::getRecipeId, recipeId)
                .eq(FdRecipeBrowseHistory::getUserId, userId);
        FdRecipeBrowseHistory existing = getOne(wrapper, false);
        Date now = new Date();
        if (existing != null) {
            existing.setBrowseTime(now);
            existing.setUpdateTime(now);
            updateById(existing);
        } else {
            FdRecipeBrowseHistory history = new FdRecipeBrowseHistory();
            history.setRecipeId(recipeId);
            history.setUserId(userId);
            history.setBrowseTime(now);
            history.setCreateTime(now);
            history.setUpdateTime(now);
            save(history);
        }
    }

    @Override
    public IPage<FdRecipeBrowseHistory> pageMyHistory(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdRecipeBrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeBrowseHistory::getUserId, userId)
                .orderByDesc(FdRecipeBrowseHistory::getBrowseTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }
}
