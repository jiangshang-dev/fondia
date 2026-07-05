package org.jeecg.modules.airag.fondia;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.llm.handler.JeecgToolsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Fondia App AI 助手：菜谱搜索工具（供大模型 Function Calling 调用）
 */
@Slf4j
@Component
public class FondiaRecipeToolsProvider implements JeecgToolsProvider {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${jeecg.fondia.search-service-name:jeecg-search}")
    private String searchServiceName;

    @Override
    public Map<ToolSpecification, ToolExecutor> getDefaultTools() {
        Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();
        JeecgLlmTools recipeTool = searchRecipeTool();
        tools.put(recipeTool.getToolSpecification(), recipeTool.getToolExecutor());
        return tools;
    }

    private JeecgLlmTools searchRecipeTool() {
        ToolSpecification spec = ToolSpecification.builder()
                .name("search_fondia_recipe")
                .description("在饭典 App 菜谱库中搜索真实菜谱。"
                        + "当用户询问某道菜怎么做、推荐菜谱、吃什么、减脂/控糖/孕期适合吃什么时，必须优先调用此工具。"
                        + "返回 JSON 数组，每项含 id、title、subtitle、coverImage、cookMinutes。"
                        + "拿到结果后：用自然语言简要介绍，并用《菜名》格式标注推荐菜；"
                        + "同时在回复末尾为每道菜追加一行卡片标记：[[recipe:菜谱ID:菜名]]，便于 App 展示可点击卡片。")
                .parameters(
                        JsonObjectSchema.builder()
                                .addStringProperty("keyword", "搜索关键词，如菜名、食材、场景（减脂晚餐、糖尿病早餐）")
                                .addIntegerProperty("limit", "返回条数，默认3，最大5")
                                .required("keyword")
                                .build()
                )
                .build();

        ToolExecutor executor = (toolExecutionRequest, memoryId) -> {
            JSONObject args = JSONObject.parseObject(toolExecutionRequest.arguments());
            String keyword = args == null ? null : args.getString("keyword");
            int limit = args == null ? 3 : args.getIntValue("limit");
            if (limit <= 0) {
                limit = 3;
            }
            if (limit > 5) {
                limit = 5;
            }
            if (oConvertUtils.isEmpty(keyword)) {
                return "[]";
            }
            try {
                String url = "http://" + searchServiceName
                        + "/search/recipe?keyword={keyword}&pageNo=1&pageSize={pageSize}";
                String body = restTemplate.getForObject(url, String.class, keyword.trim(), limit);
                if (oConvertUtils.isEmpty(body)) {
                    return "[]";
                }
                JSONObject root = JSON.parseObject(body);
                JSONObject result = root.getJSONObject("result");
                if (result == null) {
                    return "[]";
                }
                JSONArray records = result.getJSONArray("records");
                if (records == null || records.isEmpty()) {
                    return "[]";
                }
                JSONArray simplified = new JSONArray();
                for (int i = 0; i < records.size(); i++) {
                    JSONObject item = records.getJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    JSONObject card = new JSONObject();
                    card.put("id", item.getString("id"));
                    card.put("title", item.getString("title"));
                    card.put("subtitle", item.getString("subtitle"));
                    card.put("coverImage", item.getString("coverImage"));
                    card.put("cookMinutes", item.getInteger("cookMinutes"));
                    simplified.add(card);
                }
                return simplified.toJSONString();
            } catch (Exception e) {
                log.warn("[FondiaRecipeTool] 搜索失败 keyword={}", keyword, e);
                return "[]";
            }
        };
        return new JeecgLlmTools(spec, executor);
    }
}
