package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.service.ICsAiService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CsAiServiceImpl implements ICsAiService {

    @Resource
    @Qualifier("csLoadBalancedRestTemplate")
    private RestTemplate restTemplate;

    @Value("${jeecg.support.ai-service-name:jeecg-system}")
    private String aiServiceName;

    @Override
    public AiChatResult chat(String appId, String content, String conversationId) {
        if (oConvertUtils.isEmpty(appId)) {
            throw new JeecgBootException("未配置AI应用ID，请在管理后台「客服配置」中设置");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("appId", appId);
        body.put("conversationId", conversationId);
        body.put("izSaveSession", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_JSON));

        String url = "http://" + aiServiceName + "/airag/chat/send";
        try {
            ResponseEntity<org.springframework.core.io.Resource> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    org.springframework.core.io.Resource.class
            );

            StringBuilder replyBuilder = new StringBuilder();
            String newConversationId = conversationId;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String payload = line.substring(5).trim();
                    if (payload.isEmpty()) {
                        continue;
                    }
                    JSONObject event = JSON.parseObject(payload);
                    if (event == null) {
                        continue;
                    }
                    if (event.containsKey("conversationId")) {
                        newConversationId = event.getString("conversationId");
                    }
                    JSONObject data = event.getJSONObject("data");
                    if (data == null) {
                        continue;
                    }
                    if (data.containsKey("message")) {
                        replyBuilder.append(data.getString("message"));
                    }
                    if (data.containsKey("content")) {
                        replyBuilder.append(data.getString("content"));
                    }
                }
            }

            String reply = replyBuilder.toString().trim();
            if (oConvertUtils.isEmpty(reply)) {
                reply = "抱歉，我暂时无法回答这个问题，您可以尝试换个问法或转接人工客服。";
            }
            return new AiChatResult(reply, newConversationId);
        } catch (Exception e) {
            log.error("调用Airag AI失败", e);
            throw new JeecgBootException("AI服务暂不可用，请稍后重试或转接人工客服");
        }
    }
}
