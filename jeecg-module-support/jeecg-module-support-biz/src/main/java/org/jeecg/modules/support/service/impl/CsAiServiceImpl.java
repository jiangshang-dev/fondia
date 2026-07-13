package org.jeecg.modules.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.utils.AiragLocalCache;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.fondia.AiragCacheUserHolder;
import org.jeecg.modules.airag.fondia.AiragRequestIdHolder;
import org.jeecg.modules.support.service.ICsAiService;
import org.jeecg.modules.support.tool.CsAiStreamToolCollector;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 客服 AI 服务（单体模式：同进程注入 IAiragChatService，不走 HTTP/Feign）。
 */
@Slf4j
@Service
public class CsAiServiceImpl implements ICsAiService {

    private static final int READ_TIMEOUT_MS = 300_000;
    private static final int POLL_INTERVAL_MS = 100;

    @Resource
    private IAiragChatService airagChatService;

    @Override
    public AiChatResult chat(String appId, String content, String conversationId, String authorization, Long userId) {
        StringBuilder replyBuilder = new StringBuilder();
        String[] conversationHolder = {conversationId};
        StreamChatResult result = streamChat(appId, content, conversationId, authorization, userId, payload -> {
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
    public StreamChatResult streamChat(String appId, String content, String conversationId, String authorization,
                                       Long userId, Consumer<String> onEvent) {
        if (oConvertUtils.isEmpty(appId)) {
            throw new JeecgBootException("未配置AI应用ID，请在管理后台「客服配置」中设置 aiAppId");
        }

        if (userId != null) {
            AiragCacheUserHolder.set("fondia_" + userId);
        }
        try {
            ChatSendParams params = new ChatSendParams();
            params.setContent(content);
            params.setAppId(appId);
            params.setConversationId(conversationId);
            params.setIzSaveSession(true);

            log.info("[CsAi] 本地调用 Airag appId={}", appId);
            SseEmitter emitter = airagChatService.send(params);
            String requestId = AiragRequestIdHolder.get();
            if (oConvertUtils.isEmpty(requestId)) {
                throw new JeecgBootException("AI服务初始化失败");
            }

            CountDownLatch done = new CountDownLatch(1);
            emitter.onCompletion(done::countDown);
            emitter.onError(e -> done.countDown());

            StringBuilder replyBuilder = new StringBuilder();
            CsAiStreamToolCollector toolCollector = new CsAiStreamToolCollector();
            String newConversationId = conversationId;
            int lastIndex = 0;
            long deadline = System.currentTimeMillis() + READ_TIMEOUT_MS;

            while (System.currentTimeMillis() < deadline) {
                List<EventData> history = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, requestId);
                if (history != null) {
                    while (lastIndex < history.size()) {
                        EventData event = history.get(lastIndex++);
                        String payload = JSON.toJSONString(event);
                        onEvent.accept(payload);
                        toolCollector.consume(payload);
                        String[] holder = {newConversationId};
                        appendMessageFromEvent(payload, replyBuilder, holder);
                        if (oConvertUtils.isNotEmpty(holder[0])) {
                            newConversationId = holder[0];
                        }
                        String eventType = event.getEvent();
                        if (EventData.EVENT_MESSAGE_END.equals(eventType)
                                || EventData.EVENT_FLOW_ERROR.equals(eventType)) {
                            done.countDown();
                        }
                    }
                }
                if (done.getCount() == 0) {
                    break;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }

            String reply = replyBuilder.toString().trim();
            return new StreamChatResult(reply, newConversationId, toolCollector.getRecipes());
        } catch (JeecgBootException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JeecgBootException("AI服务调用被中断");
        } catch (Exception e) {
            log.error("[CsAi] 本地调用 Airag 失败 appId={}", appId, e);
            throw new JeecgBootException("AI服务暂不可用：" + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        } finally {
            AiragRequestIdHolder.clear();
            AiragCacheUserHolder.clear();
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
        if (!EventData.EVENT_MESSAGE.equals(eventType) && !EventData.EVENT_THINKING.equals(eventType)) {
            return;
        }
        JSONObject data = event.getJSONObject("data");
        if (data == null) {
            return;
        }
        if (EventData.EVENT_THINKING.equals(eventType)) {
            return;
        }
        if (data.containsKey("message")) {
            replyBuilder.append(data.getString("message"));
        }
        if (data.containsKey("content")) {
            replyBuilder.append(data.getString("content"));
        }
    }
}
