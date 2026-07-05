package org.jeecg.modules.cuisine.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.boot.starter.rabbitmq.client.RabbitMqClient;
import org.jeecg.common.constant.RabbitConstant;
import org.jeecg.common.dto.RecipeSearchSyncMessage;
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

    @Resource
    private IRecipeVideoService recipeVideoService;

    @Resource
    private RabbitMqClient rabbitMqClient;

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
        String storedVideo = recipe.getVideoUrl();
        return RecipeDetailVO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .subtitle(recipe.getSubtitle())
                .description(recipe.getDescription())
                .coverImage(recipe.getCoverImage())
                .videoUrl(storedVideo)
                .videoId(storedVideo)
                .playUrl(recipeVideoService.resolvePlayUrl(storedVideo))
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
        FdRecipe recipe = getById(recipeId);
        if (recipe == null) {
            throw new JeecgBootException("菜谱不存在");
        }
        FdRecipeCategory cuisine = categoryService.getById(recipe.getCuisineId());
        FdRecipeCategory category = categoryService.getById(recipe.getCategoryId());
        String storedVideo = recipe.getVideoUrl();
        RecipeAdminDetailVO detail = new RecipeAdminDetailVO();
        detail.setId(recipe.getId());
        detail.setTitle(recipe.getTitle());
        detail.setSubtitle(recipe.getSubtitle());
        detail.setDescription(recipe.getDescription());
        detail.setCoverImage(recipe.getCoverImage());
        detail.setVideoUrl(storedVideo);
        detail.setVideoId(storedVideo);
        detail.setPlayUrl(recipeVideoService.resolvePlayUrl(storedVideo));
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
        publishRecipeSearchSync(recipeId);
        return recipeId;
    }

    @Override
    public void publishRecipeSearchSync(Long recipeId) {
        RecipeSearchSyncMessage message = buildSearchSyncMessage(recipeId, RecipeSearchSyncMessage.ACTION_SAVE);
        if (message != null) {
            rabbitMqClient.sendMessage(RabbitConstant.SAVE_RECIPE, JSONUtil.toJsonStr(message));
        }
    }

    @Override
    public void removeRecipeFromSearch(Long recipeId) {
        RecipeSearchSyncMessage message = new RecipeSearchSyncMessage();
        message.setAction(RecipeSearchSyncMessage.ACTION_DELETE);
        message.setId(String.valueOf(recipeId));
        rabbitMqClient.sendMessage(RabbitConstant.SAVE_RECIPE, JSONUtil.toJsonStr(message));
    }

    @Override
    public int rebuildAllRecipeSearchIndex() {
        List<FdRecipe> recipes = list(new LambdaQueryWrapper<FdRecipe>()
                .eq(FdRecipe::getStatus, 1)
                .eq(FdRecipe::getDelFlag, 0));
        for (FdRecipe recipe : recipes) {
            publishRecipeSearchSync(recipe.getId());
        }
        return recipes.size();
    }

    private RecipeSearchSyncMessage buildSearchSyncMessage(Long recipeId, String action) {
        FdRecipe recipe = getById(recipeId);
        if (recipe == null) {
            return null;
        }
        FdRecipeCategory cuisine = categoryService.getById(recipe.getCuisineId());
        List<String> tags = tagService.listTagNamesByRecipeId(recipeId);
        List<String> ingredients = ingredientService.listByRecipeId(recipeId).stream()
                .map(FdRecipeIngredient::getName)
                .filter(name -> oConvertUtils.isNotEmpty(name))
                .collect(Collectors.toList());

        RecipeSearchSyncMessage message = new RecipeSearchSyncMessage();
        message.setAction(action);
        message.setId(String.valueOf(recipeId));
        message.setTitle(recipe.getTitle());
        message.setSubtitle(recipe.getSubtitle());
        message.setCoverImage(recipe.getCoverImage());
        message.setCuisineId(recipe.getCuisineId() == null ? null : String.valueOf(recipe.getCuisineId()));
        message.setCuisineName(cuisine != null ? cuisine.getName() : null);
        message.setCategoryId(recipe.getCategoryId() == null ? null : String.valueOf(recipe.getCategoryId()));
        message.setStatus(recipe.getStatus());
        message.setDifficulty(recipe.getDifficulty());
        message.setCookMinutes(recipe.getCookMinutes());
        message.setLikeCount(recipe.getLikeCount());
        message.setTags(tags);
        message.setIngredients(ingredients);
        message.setSearchText(buildSearchText(message));
        return message;
    }

    private String buildSearchText(RecipeSearchSyncMessage message) {
        StringBuilder sb = new StringBuilder();
        appendSearchToken(sb, message.getTitle());
        appendSearchToken(sb, message.getSubtitle());
        appendSearchToken(sb, message.getCuisineName());
        if (message.getTags() != null) {
            message.getTags().forEach(tag -> appendSearchToken(sb, tag));
        }
        if (message.getIngredients() != null) {
            message.getIngredients().forEach(item -> appendSearchToken(sb, item));
        }
        return sb.toString().trim();
    }

    private void appendSearchToken(StringBuilder sb, String token) {
        if (oConvertUtils.isEmpty(token)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(token.trim());
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
            ingredientService.saveOrUpdate(entity);
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
            stepService.saveOrUpdate(entity);
            index++;
        }
    }

    private void saveTagRelations(Long recipeId, List<Long> tagIds, Date now) {
        tagRelMapper.physicalDeleteByRecipeId(recipeId);
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
