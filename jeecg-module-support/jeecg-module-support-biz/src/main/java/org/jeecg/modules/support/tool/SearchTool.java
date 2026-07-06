package org.jeecg.modules.support.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.api.CsSearchFeignClient;
import org.jeecg.modules.support.api.vo.RecipeSearchFeignVO;
import org.jeecg.modules.support.vo.AiRecipeCardVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 食谱搜索 Tool：调用 jeecg-search（ES）查询 App 内菜谱。
 * 供 AI 对话工具调用结果补全、APP_LINK 跳转链接构建等场景使用。
 */
@Slf4j
@Component
public class SearchTool {

    public static final String TOOL_NAME = "search_fondia_recipe";
    public static final String RECIPE_URL_SCHEME = "fondia://recipe/";

    @Resource
    private CsSearchFeignClient searchFeignClient;

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
            Result<Page<RecipeSearchFeignVO>> result =
                    searchFeignClient.searchRecipe(query.trim(), 1, pageSize);
            if (result == null || !result.isSuccess() || result.getResult() == null) {
                return List.of();
            }
            List<RecipeSearchFeignVO> records = result.getResult().getRecords();
            if (records == null || records.isEmpty()) {
                return List.of();
            }
            List<AiRecipeCardVO> cards = new ArrayList<>();
            for (RecipeSearchFeignVO record : records) {
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

    private AiRecipeCardVO toCard(RecipeSearchFeignVO record) {
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
