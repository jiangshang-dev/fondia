package org.jeecg.modules.support.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.vo.AiRecipeCardVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 累积 Airag SSE 流中菜谱搜索 Tool 的返回结果（参考 ruiyada AgentScopeServiceImpl#buildToolResultLinkFlux）。
 * 在 AI 文本流结束后、APP_META 结束前，将结果以 APP_LINK 推送给前端。
 */
@Slf4j
public class CsAiStreamToolCollector {

    private static final Pattern TOOL_TAG = Pattern.compile(":{2,3}jeecgToolExec:{2,3}(.*?):{2,3}", Pattern.DOTALL);
    private static final String RECIPE_TOOL_NAME = "search_fondia_recipe";

    private final Map<String, AiRecipeCardVO> recipes = new LinkedHashMap<>();

    public void consume(String payload) {
        if (oConvertUtils.isEmpty(payload)) {
            return;
        }
        try {
            JSONObject event = JSON.parseObject(payload);
            if (event == null) {
                return;
            }
            String eventType = event.getString("event");
            if (!"TOOL_EXEC_DONE".equals(eventType) && !"MESSAGE".equals(eventType)) {
                return;
            }
            JSONObject data = event.getJSONObject("data");
            if (data == null) {
                return;
            }
            String message = data.getString("message");
            if (oConvertUtils.isEmpty(message)) {
                return;
            }
            if (message.contains("jeecgToolExec")) {
                Matcher matcher = TOOL_TAG.matcher(message);
                while (matcher.find()) {
                    parseToolJson(matcher.group(1));
                }
                return;
            }
            if ("TOOL_EXEC_DONE".equals(eventType) && message.trim().startsWith("{")) {
                parseToolJson(message);
            }
        } catch (Exception e) {
            log.debug("[CsAiStreamToolCollector] 解析 Tool 事件失败", e);
        }
    }

    private void parseToolJson(String toolJson) {
        if (oConvertUtils.isEmpty(toolJson)) {
            return;
        }
        try {
            JSONObject vo = JSON.parseObject(toolJson.trim());
            if (vo == null) {
                return;
            }
            String name = vo.getString("name");
            if (oConvertUtils.isNotEmpty(name) && !RECIPE_TOOL_NAME.equals(name) && !"查询食谱".equals(name)) {
                return;
            }
            if (Boolean.TRUE.equals(vo.getBoolean("hasError"))) {
                return;
            }
            String output = vo.getString("output");
            if (oConvertUtils.isEmpty(output) || "[]".equals(output.trim())) {
                return;
            }
            appendRecipeOutput(output);
        } catch (Exception e) {
            log.debug("[CsAiStreamToolCollector] 解析 Tool JSON 失败: {}", toolJson, e);
        }
    }

    private void appendRecipeOutput(String output) {
        String trimmed = output.trim();
        if (trimmed.startsWith("[")) {
            JSONArray arr = JSON.parseArray(trimmed);
            if (arr == null) {
                return;
            }
            for (int i = 0; i < arr.size(); i++) {
                addCard(arr.getJSONObject(i));
            }
            return;
        }
        if (trimmed.startsWith("{")) {
            addCard(JSON.parseObject(trimmed));
        }
    }

    private void addCard(JSONObject item) {
        if (item == null) {
            return;
        }
        String id = item.getString("id");
        if (!isValidRecipeId(id)) {
            return;
        }
        AiRecipeCardVO card = AiRecipeCardVO.builder()
                .id(id)
                .title(item.getString("title"))
                .subtitle(item.getString("subtitle"))
                .coverImage(item.getString("coverImage"))
                .cookMinutes(item.getInteger("cookMinutes"))
                .url(SearchTool.buildRecipeUrl(id))
                .build();
        recipes.putIfAbsent(id, card);
    }

    private static boolean isValidRecipeId(String id) {
        return oConvertUtils.isNotEmpty(id) && id.matches("\\d{10,20}");
    }

    public List<AiRecipeCardVO> getRecipes() {
        return new ArrayList<>(recipes.values());
    }

    public boolean isEmpty() {
        return recipes.isEmpty();
    }
}
