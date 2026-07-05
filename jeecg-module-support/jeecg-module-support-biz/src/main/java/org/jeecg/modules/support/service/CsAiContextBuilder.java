package org.jeecg.modules.support.service;

import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.enums.CsSenderType;

import java.util.List;

/**
 * 将 fd_cs_message 历史拼入本轮 Prompt，解决 App 用户 Token 无法在 Airag 侧建立 Redis 会话的问题。
 */
public final class CsAiContextBuilder {

    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_MESSAGE_CHARS = 600;

    private CsAiContextBuilder() {
    }

    public static String buildPrompt(List<FdCsMessage> history, String currentUserContent) {
        if (oConvertUtils.isEmpty(currentUserContent)) {
            return currentUserContent;
        }
        if (history == null || history.isEmpty()) {
            return currentUserContent;
        }
        int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史（必须结合上下文回答；用户问「上面/刚才/之前/刚说的」时务必引用历史，禁止说没有记录）】\n");
        for (int i = start; i < history.size(); i++) {
            FdCsMessage msg = history.get(i);
            if (msg == null || oConvertUtils.isEmpty(msg.getContent())) {
                continue;
            }
            String role = CsSenderType.USER.equals(msg.getSenderType()) ? "用户" : "助手";
            sb.append(role).append("：").append(trim(msg.getContent())).append("\n\n");
        }
        sb.append("【用户当前问题】\n").append(currentUserContent.trim());
        return sb.toString();
    }

    private static String trim(String text) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        if (t.length() <= MAX_MESSAGE_CHARS) {
            return t;
        }
        return t.substring(0, MAX_MESSAGE_CHARS) + "...";
    }
}
