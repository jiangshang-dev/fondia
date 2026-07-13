package org.jeecg.modules.support.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.service.IRecipeSearchService;
import org.jeecg.modules.support.vo.AiRecipeCardVO;
import org.jeecg.modules.vo.RecipeSearchItemVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 食谱搜索 Tool：调用 search 模块（ES）查询 App 内菜谱。
 */
@Slf4j
@Component
public class SearchTool {

    public static final String TOOL_NAME = "search_fondia_recipe";
    public static final String RECIPE_URL_SCHEME = "fondia://recipe/";

    @Resource
    private IRecipeSearchService recipeSearchService;

    /**
     * 查询食谱，返回 JSON 数组字符串（与 Airag search_fondia_recipe Tool 输出格式一致）。
     */
    public String searchRecipes(String query, Integer limit) {
        List<AiRecipeCardVO> cards = searchAsCards(query, limit);
        if (cards.isEmpty()) {
            return "[]";
        }
        JSONArray arr = new JSONArray();
        for (AiRecipeCardVO card : cards) {
            JSONObject item = new JSONObject();
            item.put("id", card.getId());
            item.put("title", card.getTitle());
            item.put("subtitle", card.getSubtitle());
            item.put("coverImage", card.getCoverImage());
            item.put("cookMinutes", card.getCookMinutes());
            item.put("url", card.getUrl());
            arr.add(item);
        }
        return arr.toJSONString();
    }

    /**
     * 查询食谱并转为卡片列表（含跳转 url）。
     */
    public List<AiRecipeCardVO> searchAsCards(String query, Integer limit) {
        if (oConvertUtils.isEmpty(query)) {
            return List.of();
        }
        int pageSize = limit == null || limit <= 0 ? 3 : Math.min(limit, 5);
        try {
            IPage<RecipeSearchItemVO> page = recipeSearchService.searchRecipes(query.trim(), 1, pageSize);
            if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
                return List.of();
            }
            List<AiRecipeCardVO> cards = new ArrayList<>();
            for (RecipeSearchItemVO record : page.getRecords()) {
                if (record == null || oConvertUtils.isEmpty(record.getId())) {
                    continue;
                }
                cards.add(toCard(record));
            }
            return cards;
        } catch (Exception e) {
            log.warn("[SearchTool] 查询食谱失败 query={}", query, e);
            return List.of();
        }
    }

    public static String buildRecipeUrl(String recipeId) {
        if (oConvertUtils.isEmpty(recipeId)) {
            return "";
        }
        return RECIPE_URL_SCHEME + recipeId;
    }

    private AiRecipeCardVO toCard(RecipeSearchItemVO record) {
        return AiRecipeCardVO.builder()
                .id(record.getId())
                .title(record.getTitle())
                .subtitle(record.getSubtitle())
                .coverImage(record.getCoverImage())
                .cookMinutes(record.getCookMinutes())
                .url(buildRecipeUrl(record.getId()))
                .build();
    }
}
