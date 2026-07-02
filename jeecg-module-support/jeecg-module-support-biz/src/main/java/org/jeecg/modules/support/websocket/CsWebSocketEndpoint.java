package org.jeecg.modules.support.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.entity.FdCsSession;
import org.jeecg.modules.support.enums.CsSenderType;
import org.jeecg.modules.support.enums.CsSessionStatus;
import org.jeecg.modules.support.service.IFdCsMessageService;
import org.jeecg.modules.support.service.IFdCsSessionService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/cs/{role}/{targetId}")
public class CsWebSocketEndpoint {

    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();
    private static final Map<String, Session> AGENT_POOL = new ConcurrentHashMap<>();

    private Session session;
    private String role;
    private String targetId;

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("role") String role,
                       @PathParam("targetId") String targetId) {
        this.session = session;
        this.role = role;
        this.targetId = targetId;
        if ("app".equals(role)) {
            SESSION_POOL.put(targetId, session);
            log.info("[CS-WS] App connected sessionId={}", targetId);
        } else if ("agent".equals(role)) {
            AGENT_POOL.put(targetId, session);
            log.info("[CS-WS] Agent connected agentId={}", targetId);
        }
    }

    @OnClose
    public void onClose() {
        if ("app".equals(role)) {
            SESSION_POOL.remove(targetId);
        } else if ("agent".equals(role)) {
            AGENT_POOL.remove(targetId);
        }
        log.info("[CS-WS] disconnected role={} targetId={}", role, targetId);
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            JSONObject json = JSON.parseObject(message);
            String cmd = json.getString("cmd");
            if ("PING".equals(cmd)) {
                sendText(session, "{\"cmd\":\"PONG\",\"timestamp\":" + System.currentTimeMillis() + "}");
                return;
            }
            if (!"CHAT_SEND".equals(cmd)) {
                return;
            }
            JSONObject data = json.getJSONObject("data");
            if (data == null) {
                return;
            }
            String content = data.getString("content");
            String clientMsgId = data.getString("clientMsgId");
            String sessionIdStr = json.getString("sessionId");

            IFdCsSessionService sessionService = SpringContextUtils.getBean(IFdCsSessionService.class);
            IFdCsMessageService messageService = SpringContextUtils.getBean(IFdCsMessageService.class);

            FdCsSession csSession = sessionService.getById(Long.valueOf(sessionIdStr));
            if (csSession == null) {
                return;
            }

            if ("app".equals(role)) {
                FdCsMessage saved = messageService.saveMessage(
                        csSession.getId(), CsSenderType.USER, String.valueOf(csSession.getUserId()), content, clientMsgId);
                csSession.setLastMessage(content);
                csSession.setLastMessageTime(System.currentTimeMillis());
                if (csSession.getAgentId() != null) {
                    csSession.setAgentUnread((csSession.getAgentUnread() == null ? 0 : csSession.getAgentUnread()) + 1);
                    messageService.pushToAgent(saved, csSession.getAgentId());
                }
                sessionService.updateById(csSession);
                ack(saved);
            } else if ("agent".equals(role)) {
                FdCsMessage saved = messageService.saveMessage(
                        csSession.getId(), CsSenderType.AGENT, targetId, content, clientMsgId);
                csSession.setLastMessage(content);
                csSession.setLastMessageTime(System.currentTimeMillis());
                csSession.setStatus(CsSessionStatus.CHATTING);
                messageService.pushToUser(saved, csSession.getId());
                sessionService.updateById(csSession);
                ack(saved);
            }
        } catch (Exception e) {
            log.error("[CS-WS] handle message error", e);
        }
    }

    private void ack(FdCsMessage message) {
        JSONObject ack = new JSONObject();
        ack.put("cmd", "CHAT_ACK");
        ack.put("sessionId", String.valueOf(message.getSessionId()));
        JSONObject data = new JSONObject();
        data.put("serverMsgId", String.valueOf(message.getId()));
        data.put("clientMsgId", message.getClientMsgId());
        ack.put("data", data);
        ack.put("timestamp", System.currentTimeMillis());
        sendText(session, ack.toJSONString());
    }

    public static void sendToSession(String sessionId, String message) {
        sendText(SESSION_POOL.get(sessionId), message);
    }

    public static void sendToAgent(String agentId, String message) {
        sendText(AGENT_POOL.get(agentId), message);
    }

    private static void sendText(Session wsSession, String message) {
        if (wsSession != null && wsSession.isOpen()) {
            wsSession.getAsyncRemote().sendText(message);
        }
    }
}
