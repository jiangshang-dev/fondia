package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.service.ICsAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class CsAiServiceImpl implements ICsAiService {

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 300_000;

    @Value("${jeecg.support.ai-service-name:jeecg-system}")
    private String aiServiceName;

    @Value("${jeecg.support.ai-chat-base-url:}")
    private String aiChatBaseUrl;

    @Override
    public AiChatResult chat(String appId, String content, String conversationId) {
        StringBuilder replyBuilder = new StringBuilder();
        String[] conversationHolder = {conversationId};
        StreamChatResult result = streamChat(appId, content, conversationId, payload -> {
            appendMessageFromEvent(payload, replyBuilder, conversationHolder);
        });
        String reply = replyBuilder.toString().trim();
        if (oConvertUtils.isEmpty(reply)) {
            reply = result.reply();
        }
        if (oConvertUtils.isEmpty(reply)) {
            reply = "抱歉，我暂时无法回答这个问题，您可以尝试换个问法或转接人工客服。";
        }
        String convId = oConvertUtils.isNotEmpty(conversationHolder[0]) ? conversationHolder[0] : result.conversationId();
        return new AiChatResult(reply, convId);
    }

    @Override
    public StreamChatResult streamChat(String appId, String content, String conversationId, Consumer<String> onEvent) {
        if (oConvertUtils.isEmpty(appId)) {
            throw new JeecgBootException("未配置AI应用ID，请在管理后台「客服配置」中设置 aiAppId");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("appId", appId);
        body.put("conversationId", conversationId);
        body.put("izSaveSession", true);

        String url = resolveChatUrl();
        String requestJson = JSON.toJSONString(body);
        log.info("[CsAi] 流式调用 Airag appId={} url={}", appId, url);

        HttpURLConnection conn = null;
        StringBuilder replyBuilder = new StringBuilder();
        String newConversationId = conversationId;
        try {
            conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "text/event-stream, application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                String errBody = readStream(conn.getErrorStream());
                log.error("[CsAi] Airag 返回 HTTP {} body={}", status, errBody);
                throw new JeecgBootException("AI服务返回异常(HTTP " + status + ")");
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String payload = line.substring(5).trim();
                    if (payload.isEmpty() || "[DONE]".equals(payload)) {
                        continue;
                    }
                    onEvent.accept(payload);
                    String[] holder = {newConversationId};
                    appendMessageFromEvent(payload, replyBuilder, holder);
                    if (oConvertUtils.isNotEmpty(holder[0])) {
                        newConversationId = holder[0];
                    }
                }
            }

            String reply = replyBuilder.toString().trim();
            return new StreamChatResult(reply, newConversationId);
        } catch (JeecgBootException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CsAi] 流式调用 Airag 失败 url={}", url, e);
            throw new JeecgBootException("AI服务暂不可用：" + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void appendMessageFromEvent(String payload, StringBuilder replyBuilder, String[] conversationHolder) {
        JSONObject event = JSON.parseObject(payload);
        if (event == null) {
            return;
        }
        if (event.containsKey("conversationId")) {
            conversationHolder[0] = event.getString("conversationId");
        }
        String eventType = event.getString("event");
        if (!"MESSAGE".equals(eventType) && !"THINKING".equals(eventType)) {
            return;
        }
        JSONObject data = event.getJSONObject("data");
        if (data == null) {
            return;
        }
        if ("THINKING".equals(eventType)) {
            return;
        }
        if (data.containsKey("message")) {
            replyBuilder.append(data.getString("message"));
        }
        if (data.containsKey("content")) {
            replyBuilder.append(data.getString("content"));
        }
    }

    private String resolveChatUrl() {
        if (oConvertUtils.isNotEmpty(aiChatBaseUrl)) {
            String base = aiChatBaseUrl.trim();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            if (base.endsWith("/airag/chat/send")) {
                return base;
            }
            return base + "/airag/chat/send";
        }
        return "http://" + aiServiceName + "/airag/chat/send";
    }

    private static String readStream(java.io.InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
