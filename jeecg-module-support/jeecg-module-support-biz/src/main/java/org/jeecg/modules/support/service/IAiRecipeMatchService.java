package org.jeecg.modules.support.service;

import org.jeecg.modules.support.vo.AiRecipeCardVO;

import java.util.List;

public interface IAiRecipeMatchService {

    List<AiRecipeCardVO> matchFromReply(String aiReply);

    /**
     * 综合用户问题、AI 回复与会话历史匹配菜谱卡片（服务端兜底，不依赖模型是否输出标记）
     */
    List<AiRecipeCardVO> matchForChat(Long sessionId, String userMessage, String aiReply);
}
