package org.jeecg.modules.interaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.interaction.entity.FdRecipeLike;
import org.jeecg.modules.interaction.vo.ToggleResultVO;

public interface IFdRecipeLikeService extends IService<FdRecipeLike> {

    ToggleResultVO toggleLike(Long recipeId, Long userId);

    boolean isLiked(Long recipeId, Long userId);

    IPage<FdRecipeLike> pageMyLikes(Long userId, Integer pageNo, Integer pageSize);
}
