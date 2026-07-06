package org.jeecg.modules.support.service;

import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.vo.AiRecipeCardVO;

import java.util.List;

public interface IFdCsMessageService {

    FdCsMessage saveMessage(Long sessionId, String senderType, String senderId, String content, String clientMsgId);

    FdCsMessage saveMessage(Long sessionId, String senderType, String senderId, String content, String clientMsgId,
                            List<AiRecipeCardVO> recipes);

    void pushToAgent(FdCsMessage message, String agentId);

    void pushToAllAgents(FdCsMessage message, Long sessionId);

    void pushToUser(FdCsMessage message, Long sessionId);
}
