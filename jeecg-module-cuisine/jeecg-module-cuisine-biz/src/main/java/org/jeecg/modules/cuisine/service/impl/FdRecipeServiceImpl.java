package org.jeecg.modules.cuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.cuisine.entity.FdRecipe;
import org.jeecg.modules.cuisine.entity.FdRecipeCategory;
import org.jeecg.modules.cuisine.entity.FdRecipeIngredient;
import org.jeecg.modules.cuisine.entity.FdRecipeStep;
import org.jeecg.modules.cuisine.entity.FdRecipeTagRel;
import org.jeecg.modules.cuisine.mapper.FdRecipeMapper;
import org.jeecg.modules.cuisine.mapper.FdRecipeTagRelMapper;
import org.jeecg.modules.cuisine.service.*;
import org.jeecg.modules.cuisine.vo.RecipeAdminDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeAdminSaveDTO;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.Date;
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
    public RecipeAdminDetailVO getRecipeAdminDetail(Long recipeId) {
        FdRecipe recipe = getById(recipeId.toString());
        if (recipe == null) {
            throw new JeecgBootException("菜谱不存在");
        }
        FdRecipeCategory cuisine = categoryService.getById(recipe.getCuisineId());
        FdRecipeCategory category = categoryService.getById(recipe.getCategoryId());
        RecipeAdminDetailVO detail = new RecipeAdminDetailVO();
        detail.setId(recipe.getId());
        detail.setTitle(recipe.getTitle());
        detail.setSubtitle(recipe.getSubtitle());
        detail.setDescription(recipe.getDescription());
        detail.setCoverImage(recipe.getCoverImage());
        detail.setVideoUrl(recipe.getVideoUrl());
        detail.setVideoDuration(recipe.getVideoDuration());
        detail.setDifficulty(recipe.getDifficulty());
        detail.setCookMinutes(recipe.getCookMinutes());
        detail.setCalories(recipe.getCalories());
        detail.setServingSize(recipe.getServingSize());
        detail.setLikeCount(recipe.getLikeCount());
        detail.setCollectCount(recipe.getCollectCount());
        detail.setCommentCount(recipe.getCommentCount());
        detail.setViewCount(recipe.getViewCount());
        detail.setCuisineId(recipe.getCuisineId());
        detail.setCuisineName(cuisine != null ? cuisine.getName() : null);
        detail.setCategoryId(recipe.getCategoryId());
        detail.setCategoryName(category != null ? category.getName() : null);
        detail.setTags(tagService.listTagNamesByRecipeId(recipeId));
        detail.setTagIds(tagService.listTagIdsByRecipeId(recipeId));
        detail.setIngredients(ingredientService.listByRecipeId(recipeId));
        detail.setSteps(stepService.listByRecipeId(recipeId));
        detail.setPublishTime(recipe.getPublishTime());
        detail.setIsRecommend(recipe.getIsRecommend());
        detail.setSortNo(recipe.getSortNo());
        detail.setStatus(recipe.getStatus());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveRecipeAdmin(RecipeAdminSaveDTO dto, boolean isUpdate) {
        if (dto.getTitle() == null || dto.getCuisineId() == null || dto.getCategoryId() == null) {
            throw new JeecgBootException("标题、菜系、分类不能为空");
        }
        Date now = new Date();
        FdRecipe recipe;
        if (isUpdate) {
            if (dto.getId() == null) {
                throw new JeecgBootException("菜谱ID不能为空");
            }
            recipe = getById(dto.getId());
            if (recipe == null) {
                throw new JeecgBootException("菜谱不存在");
            }
        } else {
            recipe = new FdRecipe();
            recipe.setCreateTime(now);
            recipe.setLikeCount(0);
            recipe.setCollectCount(0);
            recipe.setCommentCount(0);
            recipe.setViewCount(0);
        }
        recipe.setCategoryId(dto.getCategoryId());
        recipe.setCuisineId(dto.getCuisineId());
        recipe.setTitle(dto.getTitle());
        recipe.setSubtitle(dto.getSubtitle());
        recipe.setDescription(dto.getDescription());
        recipe.setCoverImage(dto.getCoverImage());
        recipe.setVideoUrl(dto.getVideoUrl());
        recipe.setVideoDuration(dto.getVideoDuration());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setCookMinutes(dto.getCookMinutes());
        recipe.setCalories(dto.getCalories());
        recipe.setServingSize(dto.getServingSize());
        recipe.setIsRecommend(dto.getIsRecommend() == null ? 0 : dto.getIsRecommend());
        recipe.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
        recipe.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        recipe.setUpdateTime(now);
        if (recipe.getStatus() == 1 && recipe.getPublishTime() == null) {
            recipe.setPublishTime(now);
        }
        if (isUpdate) {
            updateById(recipe);
        } else {
            save(recipe);
        }
        Long recipeId = recipe.getId();
        saveIngredients(recipeId, dto.getIngredients(), now);
        saveSteps(recipeId, dto.getSteps(), now);
        saveTagRelations(recipeId, dto.getTagIds(), now);
        return recipeId;
    }

    private void saveIngredients(Long recipeId, List<FdRecipeIngredient> ingredients, Date now) {
        ingredientService.remove(new LambdaQueryWrapper<FdRecipeIngredient>()
                .eq(FdRecipeIngredient::getRecipeId, recipeId));
        if (CollectionUtils.isEmpty(ingredients)) {
            return;
        }
        int index = 0;
        for (FdRecipeIngredient item : ingredients) {
            if (item == null || oConvertUtils.isEmpty(item.getName())) {
                continue;
            }
            FdRecipeIngredient entity = new FdRecipeIngredient();
            entity.setRecipeId(recipeId);
            entity.setName(item.getName());
            entity.setAmount(item.getAmount());
            entity.setImage(item.getImage());
            entity.setSortNo(item.getSortNo() != null ? item.getSortNo() : index);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            ingredientService.save(entity);
            index++;
        }
    }

    private void saveSteps(Long recipeId, List<FdRecipeStep> steps, Date now) {
        stepService.remove(new LambdaQueryWrapper<FdRecipeStep>()
                .eq(FdRecipeStep::getRecipeId, recipeId));
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        int index = 0;
        for (FdRecipeStep item : steps) {
            if (item == null || oConvertUtils.isEmpty(item.getContent())) {
                continue;
            }
            FdRecipeStep entity = new FdRecipeStep();
            entity.setRecipeId(recipeId);
            entity.setStepNo(item.getStepNo() != null ? item.getStepNo() : index + 1);
            entity.setImage(item.getImage());
            entity.setContent(item.getContent());
            entity.setTip(item.getTip());
            entity.setSortNo(item.getSortNo() != null ? item.getSortNo() : index);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            stepService.save(entity);
            index++;
        }
    }

    private void saveTagRelations(Long recipeId, List<Long> tagIds, Date now) {
        tagRelMapper.delete(new LambdaQueryWrapper<FdRecipeTagRel>()
                .eq(FdRecipeTagRel::getRecipeId, recipeId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        for (Long tagId : tagIds) {
            if (tagId == null) {
                continue;
            }
            FdRecipeTagRel rel = new FdRecipeTagRel();
            rel.setRecipeId(recipeId);
            rel.setTagId(tagId);
            rel.setCreateTime(now);
            rel.setUpdateTime(now);
            tagRelMapper.insert(rel);
        }
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
