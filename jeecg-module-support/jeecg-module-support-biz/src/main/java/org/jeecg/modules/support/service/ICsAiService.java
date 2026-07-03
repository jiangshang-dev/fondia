package org.jeecg.modules.support.service;

import java.util.function.Consumer;

public interface ICsAiService {

    /**
     * 调用 Jeecg Airag 知识库问答，同步返回完整回复
     */
    AiChatResult chat(String appId, String content, String conversationId);

    /**
     * 流式调用 Airag，将每条 SSE data 载荷回调给上层（JSON 字符串，与 AiragChatController 一致）
     */
    StreamChatResult streamChat(String appId, String content, String conversationId, Consumer<String> onEvent);

    record AiChatResult(String reply, String conversationId) {
    }

    record StreamChatResult(String reply, String conversationId) {
    }
}
