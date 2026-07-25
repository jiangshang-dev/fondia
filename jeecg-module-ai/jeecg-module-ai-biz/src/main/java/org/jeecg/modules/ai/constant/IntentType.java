package org.jeecg.modules.ai.constant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 对话意图常量，必须与 application-ai.yml 中：
 * - material.ai.intent.keywords 的 key
 * - material.ai.mcp-servers[].intents
 * 保持一致。
 */
public final class IntentType {

    public static final String WEATHER = "weather";
    public static final String WEB_SEARCH = "web_search";
    public static final String WEB_12306 = "web_12306";
    public static final String SUANMING = "suanming";
    public static final String HUANGLI = "huangli";
    public static final String BAZI = "bazi";
    public static final String LOVE = "love";
    public static final String CHAT = "chat";
    public static final String UNKNOWN = "unknown";

    /** 业务意图（不含 chat/unknown），供 LLM 分类与校验 */
    public static final List<String> ALL_BUSINESS = List.of(
            WEATHER,
            WEB_SEARCH,
            WEB_12306,
            SUANMING,
            HUANGLI,
            BAZI,
            LOVE);

    private IntentType() {}

    /** LLM 分类可选意图集合 */
    public static Set<String> classifyCandidates() {
        Set<String> set = new LinkedHashSet<>(ALL_BUSINESS);
        set.add(CHAT);
        return set;
    }

    public static boolean isKnown(String intent) {
        if (intent == null || intent.isBlank()) {
            return false;
        }
        String value = intent.trim();
        return ALL_BUSINESS.contains(value) || CHAT.equals(value);
    }
}
