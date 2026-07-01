package org.jeecg.modules.cuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.cuisine.entity.FdRecipe;
import org.jeecg.modules.cuisine.entity.FdRecipeCategory;
import org.jeecg.modules.cuisine.entity.FdRecipeTagRel;
import org.jeecg.modules.cuisine.mapper.FdRecipeMapper;
import org.jeecg.modules.cuisine.mapper.FdRecipeTagRelMapper;
import org.jeecg.modules.cuisine.service.*;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FdRecipeServiceImpl extends ServiceImpl<FdRecipeMapper, FdRecipe>
        implements IFdRecipeService {

    @Resource
    private IFdRecipeCategoryService categoryService;

    @Resource
    private IFdRecipeTagService tagService;

    @Resource
    private IFdRecipeIngredientService ingredientService;

    @Resource
    private IFdRecipeStepService stepService;

    @Resource
    private FdRecipeTagRelMapper tagRelMapper;

    @Override
    public IPage<RecipeListItemVO> pageRecipes(Long cuisineId, Long categoryId, Integer subType,
                                               Long tagId, String keyword,
                                               Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdRecipe> wrapper = buildRecipeQuery(cuisineId, categoryId, subType, tagId, keyword);
        IPage<FdRecipe> page = page(new Page<>(pageNo, pageSize), wrapper);
        return page.convert(this::toListItem);
    }

    @Override
    public List<RecipeListItemVO> listByCategoryId(Long categoryId, Integer limit) {
        LambdaQueryWrapper<FdRecipe> wrapper = buildRecipeQuery(null, categoryId, null, null, null);
        wrapper.last("LIMIT " + (limit == null || limit <= 0 ? 8 : limit));
        return list(wrapper).stream().map(this::toListItem).collect(Collectors.toList());
    }

    @Override
    public RecipeDetailVO getRecipeDetail(Long recipeId) {
        FdRecipe recipe = getEnabledRecipe(recipeId);
        FdRecipeCategory cuisine = categoryService.getById(recipe.getCuisineId());
        return RecipeDetailVO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .subtitle(recipe.getSubtitle())
                .description(recipe.getDescription())
                .coverImage(recipe.getCoverImage())
                .videoUrl(recipe.getVideoUrl())
                .videoDuration(recipe.getVideoDuration())
                .difficulty(recipe.getDifficulty())
                .cookMinutes(recipe.getCookMinutes())
                .calories(recipe.getCalories())
                .servingSize(recipe.getServingSize())
                .likeCount(recipe.getLikeCount())
                .collectCount(recipe.getCollectCount())
                .commentCount(recipe.getCommentCount())
                .viewCount(recipe.getViewCount())
                .cuisineId(recipe.getCuisineId())
                .cuisineName(cuisine != null ? cuisine.getName() : null)
                .categoryId(recipe.getCategoryId())
                .tags(tagService.listTagNamesByRecipeId(recipeId))
                .ingredients(ingredientService.listByRecipeId(recipeId))
                .steps(stepService.listByRecipeId(recipeId))
                .publishTime(recipe.getPublishTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long recipeId) {
        FdRecipe recipe = getEnabledRecipe(recipeId);
        int viewCount = recipe.getViewCount() == null ? 0 : recipe.getViewCount();
        recipe.setViewCount(viewCount + 1);
        updateById(recipe);
    }

    private LambdaQueryWrapper<FdRecipe> buildRecipeQuery(Long cuisineId, Long categoryId, Integer subType,
                                                          Long tagId, String keyword) {
        LambdaQueryWrapper<FdRecipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipe::getStatus, 1);
        if (cuisineId != null) {
            wrapper.eq(FdRecipe::getCuisineId, cuisineId);
        }
        if (categoryId != null) {
            wrapper.eq(FdRecipe::getCategoryId, categoryId);
        }
        if (subType != null) {
            List<FdRecipeCategory> subCategories = categoryService.list(new LambdaQueryWrapper<FdRecipeCategory>()
                    .eq(FdRecipeCategory::getSubType, subType)
                    .eq(cuisineId != null, FdRecipeCategory::getParentId, cuisineId));
            if (subCategories.isEmpty()) {
                wrapper.eq(FdRecipe::getId, -1L);
            } else {
                wrapper.in(FdRecipe::getCategoryId,
                        subCategories.stream().map(FdRecipeCategory::getId).collect(Collectors.toList()));
            }
        }
        if (tagId != null) {
            LambdaQueryWrapper<FdRecipeTagRel> relWrapper = new LambdaQueryWrapper<>();
            relWrapper.eq(FdRecipeTagRel::getTagId, tagId);
            List<FdRecipeTagRel> rels = tagRelMapper.selectList(relWrapper);
            if (rels.isEmpty()) {
                wrapper.eq(FdRecipe::getId, -1L);
            } else {
                wrapper.in(FdRecipe::getId,
                        rels.stream().map(FdRecipeTagRel::getRecipeId).collect(Collectors.toList()));
            }
        }
        if (oConvertUtils.isNotEmpty(keyword)) {
            wrapper.and(w -> w.like(FdRecipe::getTitle, keyword)
                    .or().like(FdRecipe::getSubtitle, keyword)
                    .or().like(FdRecipe::getDescription, keyword));
        }
        wrapper.orderByAsc(FdRecipe::getSortNo).orderByDesc(FdRecipe::getPublishTime);
        return wrapper;
    }

    private FdRecipe getEnabledRecipe(Long recipeId) {
        FdRecipe recipe = getById(recipeId);
        if (recipe == null) {
            throw new JeecgBootException("菜谱不存在");
        }
        if (recipe.getStatus() == null || recipe.getStatus() != 1) {
            throw new JeecgBootException("菜谱已下架");
        }
        return recipe;
    }

    private RecipeListItemVO toListItem(FdRecipe recipe) {
        return RecipeListItemVO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .subtitle(recipe.getSubtitle())
                .coverImage(recipe.getCoverImage())
                .difficulty(recipe.getDifficulty())
                .cookMinutes(recipe.getCookMinutes())
                .likeCount(recipe.getLikeCount())
                .tags(tagService.listTagNamesByRecipeId(recipe.getId()))
                .build();
    }
}
