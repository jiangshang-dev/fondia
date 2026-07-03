package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.service.IAiRecipeMatchService;
import org.jeecg.modules.support.vo.AiRecipeCardVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiRecipeMatchServiceImpl implements IAiRecipeMatchService {

    private static final Pattern NUMBERED = Pattern.compile("^[\\d]+[.、)\\]]\\s*(.+)$");
    private static final Pattern BULLET = Pattern.compile("^[-*•]\\s*(.+)$");
    private static final Pattern BRACKET = Pattern.compile("[《【]([^《》【】]+)[》】]");

    @Resource
    @Qualifier("csLoadBalancedRestTemplate")
    private RestTemplate restTemplate;

    @Value("${jeecg.support.search-service-name:jeecg-search}")
    private String searchServiceName;

    @Override
    public List<AiRecipeCardVO> matchFromReply(String aiReply) {
        if (oConvertUtils.isEmpty(aiReply)) {
            return List.of();
        }
        List<String> candidates = extractCandidates(aiReply);
        Map<String, AiRecipeCardVO> matched = new LinkedHashMap<>();
        for (String name : candidates) {
            if (matched.size() >= 5) {
                break;
            }
            AiRecipeCardVO card = searchRecipe(name);
            if (card != null && !matched.containsKey(card.getId())) {
                matched.put(card.getId(), card);
            }
        }
        return new ArrayList<>(matched.values());
    }

    private List<String> extractCandidates(String text) {
        Set<String> names = new LinkedHashSet<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher numbered = NUMBERED.matcher(trimmed);
            if (numbered.find()) {
                names.add(cleanName(numbered.group(1)));
                continue;
            }
            Matcher bullet = BULLET.matcher(trimmed);
            if (bullet.find()) {
                names.add(cleanName(bullet.group(1)));
                continue;
            }
            Matcher bracket = BRACKET.matcher(trimmed);
            while (bracket.find()) {
                names.add(cleanName(bracket.group(1)));
            }
        }
        return new ArrayList<>(names);
    }

    private String cleanName(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim();
        name = name.replaceAll("[：:].*$", "");
        name = name.replaceAll("[（(].*?[）)]", "");
        name = name.replaceAll("[~～].*$", "");
        name = name.replaceAll("\\s+", "");
        return name.length() > 20 ? name.substring(0, 20) : name;
    }

    private AiRecipeCardVO searchRecipe(String keyword) {
        if (oConvertUtils.isEmpty(keyword) || keyword.length() < 2) {
            return null;
        }
        try {
            String url = "http://" + searchServiceName + "/search/recipe?keyword={keyword}&pageNo=1&pageSize=3";
            String body = restTemplate.getForObject(url, String.class, keyword);
            if (oConvertUtils.isEmpty(body)) {
                return null;
            }
            JSONObject root = JSON.parseObject(body);
            JSONObject result = root.getJSONObject("result");
            if (result == null) {
                return null;
            }
            JSONArray records = result.getJSONArray("records");
            if (records == null || records.isEmpty()) {
                return null;
            }
            JSONObject first = records.getJSONObject(0);
            return AiRecipeCardVO.builder()
                    .id(first.getString("id"))
                    .title(first.getString("title"))
                    .subtitle(first.getString("subtitle"))
                    .coverImage(first.getString("coverImage"))
                    .cookMinutes(first.getInteger("cookMinutes"))
                    .build();
        } catch (Exception e) {
            log.warn("[AiRecipeMatch] 搜索菜谱失败 keyword={}", keyword, e);
            return null;
        }
    }
}
