package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdRecipeTag;
import org.jeecg.modules.cuisine.vo.TagBriefVO;

import java.util.List;

public interface IFdRecipeTagService extends IService<FdRecipeTag> {

    List<TagBriefVO> listEnabledTags(String tagType);

    List<String> listTagNamesByRecipeId(Long recipeId);

    List<Long> listTagIdsByRecipeId(Long recipeId);
}
