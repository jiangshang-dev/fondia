package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.enums.CsSenderType;
import org.jeecg.modules.support.mapper.FdCsMessageMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.support.api.CsSearchFeignClient;
import org.jeecg.modules.support.api.vo.RecipeSearchFeignVO;
import org.jeecg.modules.support.service.IAiRecipeMatchService;
import org.jeecg.modules.support.tool.SearchTool;
import org.jeecg.modules.support.vo.AiRecipeCardVO;
import org.springframework.stereotype.Service;

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
    private static final Pattern RECIPE_TAG = Pattern.compile("\\[\\[recipe:([^:\\]]+):([^\\]]+)\\]\\]");
    private static final Pattern BRACKET = Pattern.compile("[《【]([^《》【】]+)[》】]");
    private static final Pattern DISH_QUERY = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z0-9]{2,12})(?:的)?(?:详细)?(?:做法|怎么做|如何做|怎样做|制作|步骤)");
    private static final Pattern DISH_IN_BOOK = Pattern.compile("《([\\u4e00-\\u9fa5a-zA-Z0-9]{2,12})》");
    private static final Pattern FOLLOW_UP = Pattern.compile("上面|刚才|之前|前面|上述|刚推荐|刚说|上面推荐|那道|那几");

    @Resource
    private CsSearchFeignClient searchFeignClient;

    @Resource
    private FdCsMessageMapper messageMapper;

    @Override
    public List<AiRecipeCardVO> matchForChat(Long sessionId, String userMessage, String aiReply) {
        Map<String, AiRecipeCardVO> matched = new LinkedHashMap<>();

        if (oConvertUtils.isNotEmpty(aiReply)) {
            collectFromReply(aiReply, matched);
        }

        String dish = extractDishFromUserMessage(userMessage);
        if (oConvertUtils.isNotEmpty(dish)) {
            addSearchResult(dish, matched);
        }

        if (isFollowUpQuestion(userMessage) && sessionId != null) {
            collectFromLastAiMessage(sessionId, matched);
        }

        if (matched.isEmpty() && oConvertUtils.isNotEmpty(aiReply)) {
            for (String name : extractCandidates(aiReply)) {
                if (matched.size() >= 3) {
                    break;
                }
                addSearchResult(name, matched);
            }
        }

        return new ArrayList<>(matched.values());
    }

    @Override
    public List<AiRecipeCardVO> matchFromReply(String aiReply) {
        return matchForChat(null, null, aiReply);
    }

    private void collectFromReply(String aiReply, Map<String, AiRecipeCardVO> matched) {
        Matcher tagMatcher = RECIPE_TAG.matcher(aiReply);
        while (tagMatcher.find()) {
            if (matched.size() >= 5) {
                break;
            }
            String id = tagMatcher.group(1).trim();
            String title = tagMatcher.group(2).trim();
            if (oConvertUtils.isNotEmpty(id)) {
                AiRecipeCardVO card = AiRecipeCardVO.builder().id(id).title(title).build();
                enrichCard(card);
                matched.put(id, card);
            }
        }
        tryParseJsonCards(aiReply, matched);
        for (String name : extractCandidates(aiReply)) {
            if (matched.size() >= 5) {
                break;
            }
            addSearchResult(name, matched);
        }
    }

    private void collectFromLastAiMessage(Long sessionId, Map<String, AiRecipeCardVO> matched) {
        LambdaQueryWrapper<FdCsMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdCsMessage::getSessionId, sessionId)
                .eq(FdCsMessage::getSenderType, CsSenderType.AI)
                .orderByDesc(FdCsMessage::getCreateTime)
                .last("LIMIT 3");
        List<FdCsMessage> list = messageMapper.selectList(wrapper);
        for (FdCsMessage msg : list) {
            if (msg == null || oConvertUtils.isEmpty(msg.getContent())) {
                continue;
            }
            collectFromReply(msg.getContent(), matched);
            if (!matched.isEmpty()) {
                return;
            }
        }
    }

    private boolean isFollowUpQuestion(String userMessage) {
        return oConvertUtils.isNotEmpty(userMessage) && FOLLOW_UP.matcher(userMessage).find();
    }

    private String extractDishFromUserMessage(String userMessage) {
        if (oConvertUtils.isEmpty(userMessage)) {
            return null;
        }
        String msg = userMessage.trim();

        Matcher book = DISH_IN_BOOK.matcher(msg);
        if (book.find()) {
            String dish = cleanName(book.group(1));
            if (isValidDishName(dish)) {
                return dish;
            }
        }

        Matcher query = DISH_QUERY.matcher(msg);
        if (query.find()) {
            String dish = cleanName(query.group(1));
            if (isValidDishName(dish)) {
                return dish;
            }
        }

        for (String name : extractCandidates(msg)) {
            if (isValidDishName(name)) {
                return name;
            }
        }
        return null;
    }

    private boolean isValidDishName(String name) {
        if (oConvertUtils.isEmpty(name) || name.length() < 2) {
            return false;
        }
        String[] stopWords = {"上面", "推荐", "详细", "步骤", "菜名", "什么", "哪些", "请问", "给出", "菜谱", "做法"};
        for (String stop : stopWords) {
            if (name.contains(stop)) {
                return false;
            }
        }
        return true;
    }

    private void addSearchResult(String keyword, Map<String, AiRecipeCardVO> matched) {
        if (matched.size() >= 5) {
            return;
        }
        AiRecipeCardVO card = searchRecipe(keyword);
        if (card != null && !matched.containsKey(card.getId())) {
            matched.put(card.getId(), card);
        }
    }

    private void enrichCard(AiRecipeCardVO card) {
        if (card == null || oConvertUtils.isEmpty(card.getId())) {
            return;
        }
        if (oConvertUtils.isNotEmpty(card.getCoverImage())) {
            return;
        }
        AiRecipeCardVO found = searchRecipe(oConvertUtils.isNotEmpty(card.getTitle()) ? card.getTitle() : card.getId());
        if (found != null) {
            card.setTitle(found.getTitle());
            card.setSubtitle(found.getSubtitle());
            card.setCoverImage(found.getCoverImage());
            card.setCookMinutes(found.getCookMinutes());
        }
    }

    private void tryParseJsonCards(String aiReply, Map<String, AiRecipeCardVO> matched) {
        if (matched.size() >= 5) {
            return;
        }
        try {
            int start = aiReply.indexOf('[');
            int end = aiReply.lastIndexOf(']');
            if (start < 0 || end <= start) {
                return;
            }
            String maybeJson = aiReply.substring(start, end + 1);
            JSONArray arr = JSON.parseArray(maybeJson);
            if (arr == null) {
                return;
            }
            for (int i = 0; i < arr.size(); i++) {
                if (matched.size() >= 5) {
                    break;
                }
                JSONObject item = arr.getJSONObject(i);
                if (item == null || oConvertUtils.isEmpty(item.getString("id"))) {
                    continue;
                }
                String id = item.getString("id");
                matched.putIfAbsent(id, AiRecipeCardVO.builder()
                        .id(id)
                        .title(item.getString("title"))
                        .subtitle(item.getString("subtitle"))
                        .coverImage(item.getString("coverImage"))
                        .cookMinutes(item.getInteger("cookMinutes"))
                        .build());
            }
        } catch (Exception ignored) {
        }
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
        Matcher bold = Pattern.compile("\\*\\*([\\u4e00-\\u9fa5a-zA-Z0-9]{2,12})\\*\\*").matcher(text);
        while (bold.find()) {
            names.add(cleanName(bold.group(1)));
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
            Result<Page<RecipeSearchFeignVO>> result = searchFeignClient.searchRecipe(keyword, 1, 3);
            if (result == null || !result.isSuccess() || result.getResult() == null) {
                return null;
            }
            List<RecipeSearchFeignVO> records = result.getResult().getRecords();
            if (records == null || records.isEmpty()) {
                return null;
            }
            RecipeSearchFeignVO best = pickBestRecord(keyword, records);
            return AiRecipeCardVO.builder()
                    .id(best.getId())
                    .title(best.getTitle())
                    .subtitle(best.getSubtitle())
                    .coverImage(best.getCoverImage())
                    .cookMinutes(best.getCookMinutes())
                    .url(SearchTool.buildRecipeUrl(best.getId()))
                    .build();
        } catch (Exception e) {
            log.warn("[AiRecipeMatch] 搜索菜谱失败 keyword={}", keyword, e);
            return null;
        }
    }

    private RecipeSearchFeignVO pickBestRecord(String keyword, List<RecipeSearchFeignVO> records) {
        for (RecipeSearchFeignVO item : records) {
            if (item == null) {
                continue;
            }
            String title = item.getTitle();
            if (title != null && (title.contains(keyword) || keyword.contains(title))) {
                return item;
            }
        }
        return records.get(0);
    }
}
