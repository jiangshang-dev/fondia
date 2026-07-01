package org.jeecg.modules.cuisine.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.cuisine.entity.FdRecipeCategory;
import org.jeecg.modules.cuisine.enums.CategoryLevelEnum;
import org.jeecg.modules.cuisine.mapper.FdRecipeCategoryMapper;
import org.jeecg.modules.cuisine.service.IFdRecipeCategoryService;
import org.jeecg.modules.cuisine.service.IFdRecipeService;
import org.jeecg.modules.cuisine.service.IFdRecipeTagService;
import org.jeecg.modules.cuisine.vo.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FdRecipeCategoryServiceImpl extends ServiceImpl<FdRecipeCategoryMapper, FdRecipeCategory>
        implements IFdRecipeCategoryService {

    private final IFdRecipeService recipeService;
    private final IFdRecipeTagService tagService;

    public FdRecipeCategoryServiceImpl(@Lazy IFdRecipeService recipeService, IFdRecipeTagService tagService) {
        this.recipeService = recipeService;
        this.tagService = tagService;
    }

    @Override
    public List<CategoryBriefVO> listHotCuisines() {
        return listCuisines(true);
    }

    @Override
    public List<CategoryBriefVO> listAllCuisines() {
        return listCuisines(null);
    }

    @Override
    public List<CategoryBriefVO> listSubCategories(Long cuisineId) {
        LambdaQueryWrapper<FdRecipeCategory> wrapper = baseSubWrapper(cuisineId);
        return list(wrapper).stream().map(this::toBrief).collect(Collectors.toList());
    }

    @Override
    public CuisineDetailVO getCuisineDetail(Long cuisineId) {
        FdRecipeCategory cuisine = getEnabledCuisine(cuisineId);
        return CuisineDetailVO.builder()
                .id(cuisine.getId())
                .name(cuisine.getName())
                .coverImage(cuisine.getCoverImage())
                .description(cuisine.getDescription())
                .featureTags(parseFeatureTags(cuisine.getFeatureTags()))
                .filterTags(tagService.listEnabledTags(null))
                .build();
    }

    @Override
    public CategoryTreeVO getCategoryTree(Long cuisineId) {
        FdRecipeCategory cuisine = getEnabledCuisine(cuisineId);
        List<FdRecipeCategory> subCategories = list(baseSubWrapper(cuisineId));
        List<CategorySectionVO> sections = new ArrayList<>();
        for (FdRecipeCategory sub : subCategories) {
            sections.add(CategorySectionVO.builder()
                    .categoryId(sub.getId())
                    .title(sub.getName())
                    .subType(sub.getSubType())
                    .recipes(recipeService.listByCategoryId(sub.getId(), 8))
                    .build());
        }
        return CategoryTreeVO.builder()
                .cuisineId(cuisine.getId())
                .cuisineName(cuisine.getName())
                .sections(sections)
                .build();
    }

    private List<CategoryBriefVO> listCuisines(Boolean hotOnly) {
        LambdaQueryWrapper<FdRecipeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCategory::getCategoryLevel, CategoryLevelEnum.CUISINE.getCode())
                .eq(FdRecipeCategory::getStatus, 1)
                .orderByAsc(FdRecipeCategory::getSortNo);
        if (Boolean.TRUE.equals(hotOnly)) {
            wrapper.eq(FdRecipeCategory::getIsHot, 1);
        }
        return list(wrapper).stream().map(this::toBrief).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<FdRecipeCategory> baseSubWrapper(Long cuisineId) {
        LambdaQueryWrapper<FdRecipeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCategory::getParentId, cuisineId)
                .eq(FdRecipeCategory::getCategoryLevel, CategoryLevelEnum.SUB.getCode())
                .eq(FdRecipeCategory::getStatus, 1)
                .orderByAsc(FdRecipeCategory::getSortNo);
        return wrapper;
    }

    private FdRecipeCategory getEnabledCuisine(Long cuisineId) {
        FdRecipeCategory cuisine = getById(cuisineId);
        if (cuisine == null || cuisine.getCategoryLevel() == null
                || CategoryLevelEnum.CUISINE.getCode() != cuisine.getCategoryLevel()) {
            throw new JeecgBootException("菜系不存在");
        }
        if (cuisine.getStatus() == null || cuisine.getStatus() != 1) {
            throw new JeecgBootException("菜系已禁用");
        }
        return cuisine;
    }

    private CategoryBriefVO toBrief(FdRecipeCategory category) {
        return CategoryBriefVO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .coverImage(category.getCoverImage())
                .subType(category.getSubType())
                .build();
    }

    private List<String> parseFeatureTags(String featureTags) {
        if (!StringUtils.hasText(featureTags)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(featureTags, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
