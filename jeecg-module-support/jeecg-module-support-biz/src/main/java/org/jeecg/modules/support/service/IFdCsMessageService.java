package org.jeecg.modules.support.service;

import org.jeecg.modules.support.entity.FdCsMessage;

public interface IFdCsMessageService {

    FdCsMessage saveMessage(Long sessionId, String senderType, String senderId, String content, String clientMsgId);

    void pushToAgent(FdCsMessage message, String agentId);

    void pushToUser(FdCsMessage message, Long sessionId);
}
