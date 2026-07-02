package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.mapper.FdCsMessageMapper;
import org.jeecg.modules.support.service.IFdCsMessageService;
import org.jeecg.modules.support.websocket.CsWebSocketEndpoint;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FdCsMessageServiceImpl implements IFdCsMessageService {

    @Resource
    private FdCsMessageMapper messageMapper;

    @Override
    public FdCsMessage saveMessage(Long sessionId, String senderType, String senderId, String content, String clientMsgId) {
        FdCsMessage message = new FdCsMessage()
                .setSessionId(sessionId)
                .setSenderType(senderType)
                .setSenderId(senderId)
                .setMsgType("TEXT")
                .setContent(content)
                .setClientMsgId(clientMsgId)
                .setCreateTime(System.currentTimeMillis());
        messageMapper.insert(message);
        return message;
    }

    @Override
    public void pushToAgent(FdCsMessage message, String agentId) {
        CsWebSocketEndpoint.sendToAgent(agentId, buildPushEnvelope(message));
    }

    @Override
    public void pushToUser(FdCsMessage message, Long sessionId) {
        CsWebSocketEndpoint.sendToSession(String.valueOf(sessionId), buildPushEnvelope(message));
    }

    private String buildPushEnvelope(FdCsMessage message) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", String.valueOf(message.getId()));
        data.put("senderType", message.getSenderType());
        data.put("msgType", message.getMsgType());
        data.put("content", message.getContent());
        data.put("createTime", message.getCreateTime());

        Map<String, Object> envelope = new HashMap<>();
        envelope.put("cmd", "CHAT_PUSH");
        envelope.put("sessionId", String.valueOf(message.getSessionId()));
        envelope.put("data", data);
        envelope.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(envelope);
    }
}
