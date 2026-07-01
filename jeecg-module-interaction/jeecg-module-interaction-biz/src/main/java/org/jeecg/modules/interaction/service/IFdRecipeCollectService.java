package org.jeecg.modules.interaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.interaction.entity.FdRecipeCollect;
import org.jeecg.modules.interaction.vo.ToggleResultVO;

public interface IFdRecipeCollectService extends IService<FdRecipeCollect> {

    ToggleResultVO toggleCollect(Long recipeId, Long userId);

    boolean isCollected(Long recipeId, Long userId);

    IPage<FdRecipeCollect> pageMyCollects(Long userId, Integer pageNo, Integer pageSize);
}
