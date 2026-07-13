package org.jeecg.modules.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.dto.RecipeSearchSyncMessage;
import org.jeecg.common.es.JeecgElasticsearchTemplate;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.constant.SearchConstant;
import org.jeecg.modules.service.IRecipeSearchService;
import org.jeecg.modules.vo.RecipeSearchItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecipeSearchServiceImpl implements IRecipeSearchService, ApplicationRunner {

    @Autowired(required = false)
    private JeecgElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void run(ApplicationArguments args) {
        initIndex();
    }

    @Override
    public void initIndex() {
        if (elasticsearchTemplate == null) {
            log.warn("[RecipeSearch] Elasticsearch 未配置，跳过索引初始化");
            return;
        }
        if (!elasticsearchTemplate.indexExists(SearchConstant.RECIPE_INDEX)) {
            boolean created = elasticsearchTemplate.createIndex(SearchConstant.RECIPE_INDEX);
            log.info("[RecipeSearch] 创建索引 {} 结果: {}", SearchConstant.RECIPE_INDEX, created);
        }
    }

    @Override
    public void syncFromMessage(String messageJson) {
        if (oConvertUtils.isEmpty(messageJson)) {
            return;
        }
        RecipeSearchSyncMessage message = JSON.parseObject(messageJson, RecipeSearchSyncMessage.class);
        syncRecipe(message);
    }

    @Override
    public void syncRecipe(RecipeSearchSyncMessage message) {
        if (message == null || oConvertUtils.isEmpty(message.getId())) {
            return;
        }
        if (elasticsearchTemplate == null) {
            log.warn("[RecipeSearch] Elasticsearch 未配置，忽略同步 recipeId={}", message.getId());
            return;
        }
        if (RecipeSearchSyncMessage.ACTION_DELETE.equals(message.getAction())
                || message.getStatus() == null || message.getStatus() != 1) {
            removeRecipe(message.getId());
            return;
        }
        JSONObject doc = (JSONObject) JSON.toJSON(message);
        doc.put("id", message.getId());
        boolean ok = elasticsearchTemplate.saveOrUpdate(
                SearchConstant.RECIPE_INDEX,
                SearchConstant.RECIPE_TYPE,
                message.getId(),
                doc);
        log.info("[RecipeSearch] 同步菜谱 id={} title={} result={}", message.getId(), message.getTitle(), ok);
    }

    @Override
    public void removeRecipe(String recipeId) {
        if (elasticsearchTemplate == null || oConvertUtils.isEmpty(recipeId)) {
            return;
        }
        boolean ok = elasticsearchTemplate.delete(SearchConstant.RECIPE_INDEX, SearchConstant.RECIPE_TYPE, recipeId);
        log.info("[RecipeSearch] 删除索引 recipeId={} result={}", recipeId, ok);
    }

    @Override
    public IPage<RecipeSearchItemVO> searchRecipes(String keyword, Integer pageNo, Integer pageSize) {
        Page<RecipeSearchItemVO> page = new Page<>(pageNo, pageSize);
        if (elasticsearchTemplate == null || oConvertUtils.isEmpty(keyword)) {
            page.setRecords(Collections.emptyList());
            page.setTotal(0);
            return page;
        }
        int from = Math.max(0, (pageNo - 1) * pageSize);

        JSONArray must = new JSONArray();
        must.add(buildKeywordQuery(keyword.trim()));
        JSONObject statusTerm = new JSONObject();
        JSONObject statusValue = new JSONObject();
        statusValue.put("status", 1);
        statusTerm.put("term", statusValue);
        must.add(statusTerm);

        JSONObject query = elasticsearchTemplate.buildBoolQuery(must, null, null);
        JSONObject body = elasticsearchTemplate.buildQuery(null, query, from, pageSize);
        JSONObject result = elasticsearchTemplate.search(SearchConstant.RECIPE_INDEX, SearchConstant.RECIPE_TYPE, body);

        JSONObject hits = result.getJSONObject("hits");
        if (hits == null) {
            page.setRecords(Collections.emptyList());
            page.setTotal(0);
            return page;
        }
        long total = 0;
        JSONObject totalObj = hits.getJSONObject("total");
        if (totalObj != null) {
            total = totalObj.getLongValue("value");
        }
        JSONArray hitArray = hits.getJSONArray("hits");
        List<RecipeSearchItemVO> records = new ArrayList<>();
        if (hitArray != null) {
            for (int i = 0; i < hitArray.size(); i++) {
                JSONObject hit = hitArray.getJSONObject(i);
                JSONObject source = hit.getJSONObject("_source");
                if (source == null) {
                    continue;
                }
                records.add(toItemVO(source));
            }
        }
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    private RecipeSearchItemVO toItemVO(JSONObject source) {
        List<String> tags = Collections.emptyList();
        JSONArray tagArray = source.getJSONArray("tags");
        if (tagArray != null) {
            tags = tagArray.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return RecipeSearchItemVO.builder()
                .id(source.getString("id"))
                .title(source.getString("title"))
                .subtitle(source.getString("subtitle"))
                .coverImage(source.getString("coverImage"))
                .cuisineName(source.getString("cuisineName"))
                .difficulty(source.getInteger("difficulty"))
                .cookMinutes(source.getInteger("cookMinutes"))
                .likeCount(source.getInteger("likeCount"))
                .tags(tags)
                .build();
    }

    /**
     * 构建关键词查询。
     * query_string + 通配符在 text 分词字段上，单字「锅」能命中，多字「锅包」无法命中（无对应 token）。
     * 改为 searchText.keyword 子串 wildcard，并对 title 等字段 match 兜底。
     */
    private JSONObject buildKeywordQuery(String keyword) {
        JSONArray should = new JSONArray();

        JSONObject wildcardWrap = new JSONObject();
        JSONObject wildcardField = new JSONObject();
        JSONObject wildcardVal = new JSONObject();
        wildcardVal.put("value", "*" + escapeWildcard(keyword) + "*");
        wildcardField.put("searchText.keyword", wildcardVal);
        wildcardWrap.put("wildcard", wildcardField);
        should.add(wildcardWrap);

        JSONObject matchWrap = new JSONObject();
        JSONObject matchSearch = new JSONObject();
        JSONObject matchField = new JSONObject();
        matchField.put("query", keyword);
        matchField.put("operator", "and");
        matchSearch.put("searchText", matchField);
        matchWrap.put("match", matchSearch);
        should.add(matchWrap);

        JSONObject matchTitleWrap = new JSONObject();
        JSONObject matchTitle = new JSONObject();
        JSONObject matchTitleField = new JSONObject();
        matchTitleField.put("query", keyword);
        matchTitleField.put("operator", "and");
        matchTitle.put("title", matchTitleField);
        matchTitleWrap.put("match", matchTitle);
        should.add(matchTitleWrap);

        JSONObject bool = new JSONObject();
        bool.put("should", should);
        bool.put("minimum_should_match", 1);
        JSONObject query = new JSONObject();
        query.put("bool", bool);
        return query;
    }

    private String escapeWildcard(String keyword) {
        return keyword.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("?", "\\?");
    }
}
