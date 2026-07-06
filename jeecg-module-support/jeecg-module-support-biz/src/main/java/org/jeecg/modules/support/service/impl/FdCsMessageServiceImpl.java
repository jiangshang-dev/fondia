package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.mapper.FdCsMessageMapper;
import org.jeecg.modules.support.service.IFdCsMessageService;
import org.jeecg.modules.support.vo.AiRecipeCardVO;
import org.jeecg.modules.support.websocket.CsWebSocketEndpoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FdCsMessageServiceImpl implements IFdCsMessageService {

    @Resource
    private FdCsMessageMapper messageMapper;

    @Override
    public FdCsMessage saveMessage(Long sessionId, String senderType, String senderId, String content, String clientMsgId) {
        return saveMessage(sessionId, senderType, senderId, content, clientMsgId, null);
    }

    @Override
    public FdCsMessage saveMessage(Long sessionId, String senderType, String senderId, String content,
                                    String clientMsgId, List<AiRecipeCardVO> recipes) {
        FdCsMessage message = new FdCsMessage()
                .setSessionId(sessionId)
                .setSenderType(senderType)
                .setSenderId(senderId)
                .setMsgType("TEXT")
                .setContent(content)
                .setExtraJson(buildExtraJson(recipes))
                .setClientMsgId(clientMsgId)
                .setCreateTime(System.currentTimeMillis());
        messageMapper.insert(message);
        return message;
    }

    static String buildExtraJson(List<AiRecipeCardVO> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return null;
        }
        JSONObject extra = new JSONObject();
        extra.put("recipes", recipes);
        return extra.toJSONString();
    }

    static List<AiRecipeCardVO> parseRecipesFromExtra(String extraJson) {
        if (oConvertUtils.isEmpty(extraJson)) {
            return List.of();
        }
        try {
            JSONObject extra = JSON.parseObject(extraJson);
            if (extra == null) {
                return List.of();
            }
            JSONArray arr = extra.getJSONArray("recipes");
            if (arr == null || arr.isEmpty()) {
                return List.of();
            }
            List<AiRecipeCardVO> list = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                JSONObject item = arr.getJSONObject(i);
                if (item == null || oConvertUtils.isEmpty(item.getString("id"))) {
                    continue;
                }
                list.add(AiRecipeCardVO.builder()
                        .id(item.getString("id"))
                        .title(item.getString("title"))
                        .subtitle(item.getString("subtitle"))
                        .coverImage(item.getString("coverImage"))
                        .cookMinutes(item.getInteger("cookMinutes"))
                        .url(item.getString("url"))
                        .build());
            }
            return list;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public void pushToAgent(FdCsMessage message, String agentId) {
        CsWebSocketEndpoint.sendToAgent(agentId, buildPushEnvelope(message));
    }

    @Override
    public void pushToAllAgents(FdCsMessage message, Long sessionId) {
        CsWebSocketEndpoint.broadcastToAgents(buildNotifyEnvelope(message, sessionId));
    }

    @Override
    public void pushToUser(FdCsMessage message, Long sessionId) {
        CsWebSocketEndpoint.sendToSession(String.valueOf(sessionId), buildPushEnvelope(message));
    }

    private String buildNotifyEnvelope(FdCsMessage message, Long sessionId) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("cmd", "SESSION_NOTIFY");
        envelope.put("sessionId", String.valueOf(sessionId));
        envelope.put("data", buildMessageData(message));
        envelope.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(envelope);
    }

    private Map<String, Object> buildMessageData(FdCsMessage message) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", String.valueOf(message.getId()));
        data.put("senderType", message.getSenderType());
        data.put("msgType", message.getMsgType());
        data.put("content", message.getContent());
        data.put("createTime", message.getCreateTime());
        List<AiRecipeCardVO> recipes = parseRecipesFromExtra(message.getExtraJson());
        if (!recipes.isEmpty()) {
            data.put("recipes", recipes);
        }
        return data;
    }

    private String buildPushEnvelope(FdCsMessage message) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("cmd", "CHAT_PUSH");
        envelope.put("sessionId", String.valueOf(message.getSessionId()));
        envelope.put("data", buildMessageData(message));
        envelope.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(envelope);
    }
}
