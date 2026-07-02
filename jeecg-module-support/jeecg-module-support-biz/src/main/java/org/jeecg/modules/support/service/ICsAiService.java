package org.jeecg.modules.support.service;

public interface ICsAiService {

    /**
     * 调用 Jeecg Airag 知识库问答，同步返回完整回复
     */
    AiChatResult chat(String appId, String content, String conversationId);

    record AiChatResult(String reply, String conversationId) {
    }
}
