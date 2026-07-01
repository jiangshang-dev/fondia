package org.jeecg.modules.interaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.interaction.entity.FdRecipeBrowseHistory;

public interface IFdRecipeBrowseHistoryService extends IService<FdRecipeBrowseHistory> {

    void recordBrowse(Long recipeId, Long userId);

    IPage<FdRecipeBrowseHistory> pageMyHistory(Long userId, Integer pageNo, Integer pageSize);
}
