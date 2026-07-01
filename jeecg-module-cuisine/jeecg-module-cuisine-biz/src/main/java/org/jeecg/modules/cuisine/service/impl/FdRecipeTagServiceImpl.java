package org.jeecg.modules.cuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.cuisine.entity.FdRecipeTag;
import org.jeecg.modules.cuisine.entity.FdRecipeTagRel;
import org.jeecg.modules.cuisine.mapper.FdRecipeTagMapper;
import org.jeecg.modules.cuisine.mapper.FdRecipeTagRelMapper;
import org.jeecg.modules.cuisine.service.IFdRecipeTagService;
import org.jeecg.modules.cuisine.vo.TagBriefVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FdRecipeTagServiceImpl extends ServiceImpl<FdRecipeTagMapper, FdRecipeTag>
        implements IFdRecipeTagService {

    @Resource
    private FdRecipeTagRelMapper tagRelMapper;

    @Override
    public List<TagBriefVO> listEnabledTags(String tagType) {
        LambdaQueryWrapper<FdRecipeTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeTag::getStatus, 1)
                .orderByAsc(FdRecipeTag::getSortNo);
        if (StringUtils.hasText(tagType)) {
            wrapper.eq(FdRecipeTag::getTagType, tagType);
        }
        return list(wrapper).stream().map(tag -> TagBriefVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .tagType(tag.getTagType())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<String> listTagNamesByRecipeId(Long recipeId) {
        LambdaQueryWrapper<FdRecipeTagRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(FdRecipeTagRel::getRecipeId, recipeId);
        List<FdRecipeTagRel> rels = tagRelMapper.selectList(relWrapper);
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = rels.stream().map(FdRecipeTagRel::getTagId).collect(Collectors.toList());
        List<FdRecipeTag> tags = listByIds(tagIds);
        Map<Long, String> tagNameMap = tags.stream()
                .collect(Collectors.toMap(FdRecipeTag::getId, FdRecipeTag::getName, (a, b) -> a));
        return tagIds.stream().map(tagNameMap::get).filter(StringUtils::hasText).collect(Collectors.toList());
    }

    @Override
    public List<Long> listTagIdsByRecipeId(Long recipeId) {
        LambdaQueryWrapper<FdRecipeTagRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(FdRecipeTagRel::getRecipeId, recipeId);
        return tagRelMapper.selectList(relWrapper).stream()
                .map(FdRecipeTagRel::getTagId)
                .collect(Collectors.toList());
    }
}
