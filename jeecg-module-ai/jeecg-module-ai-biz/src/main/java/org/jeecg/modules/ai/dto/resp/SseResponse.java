package org.jeecg.modules.ai.dto.resp;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeecg.modules.ai.vo.MessageDataVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
public class SseResponse<T> {
    private String type; // 消息类型: chunk/end/error
    private String conversationId; // 消息类型: chunk/end/error
    private T data;      // 业务数据

    public SseResponse(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public SseResponse(String type, String conversationId, T data) {
        this.type = type;
        this.conversationId = conversationId;
        this.data = data;
    }

    // 静态工厂方法
    public static <T> SseResponse<T> chunk(String conversationId, T data) {
        return new SseResponse<>("message", conversationId, data);
    }

    public static <T> SseResponse<T> chunkLink(String conversationId, T data) {
        return new SseResponse<>("link", conversationId, data);
    }

    /** 模型思考过程片段 */
    public static <T> SseResponse<T> chunkThinking(String conversationId, T data) {
        return new SseResponse<>("thinking", conversationId, data);
    }

    /** 工具调用 / 工具执行过程 */
    public static <T> SseResponse<T> chunkTool(String conversationId, T data) {
        return new SseResponse<>("tool", conversationId, data);
    }

    public static <T> SseResponse<T> success(T data) {
        return new SseResponse<>("end", data);
    }

    public static <T> SseResponse<T> progress(T data) {
        return new SseResponse<>("notify", data);
    }

    public static <T> SseResponse<T> heartbeat(T data) {
        return new SseResponse<>("heartbeat", data);
    }

    public static SseResponse<JSONObject> end(String userMsgId, String assistantMsgId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("userMsgId", userMsgId);
        jsonObject.set("assistantMsgId", assistantMsgId);
        return new SseResponse<>("end", jsonObject);
    }

    public static SseResponse<MessageDataVO<Object>> endFlux(String userMsgId, String assistantMsgId) {
        MessageDataVO<Object> messageDataVO = MessageDataVO.builder()
                .userMsgId(userMsgId)
                .assistantMsgId(assistantMsgId)
                .build();
        return new SseResponse<>("end", messageDataVO);
    }

    public static SseResponse<String> error(String message) {
        return new SseResponse<>("error", message);
    }

    /**
     * 添加中断方法
     *
     * @param conversationId 会话ID
     * @param message        中断信息
     */
    public static SseResponse<String> interrupted(String conversationId, String message) {
        return new SseResponse<>("interrupted", conversationId, message);
    }


    public static void sendEmptyHeartbeat(SseEmitter emitter) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // 每10秒发送一次心跳
        scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .data(SseResponse.heartbeat("heartbeat"))
                        .reconnectTime(10000L));
            } catch (Exception e) {
                // 如果发送失败，说明连接已断开，关闭调度器
                scheduler.shutdown();
            }
        }, 15, 15, TimeUnit.SECONDS);

        // 注册销毁回调
        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(scheduler::shutdown);
        emitter.onError((e) -> scheduler.shutdown());
    }

    public static void sendDocumentError(SseEmitter emitter, String documentId, String message) throws IOException {
        emitter.send(SseEmitter.event().data(SseResponse.error(documentId + "-" + message)));
    }

    public static void sentError(SseEmitter emitter, String message) throws IOException {
        emitter.send(SseEmitter.event().data(SseResponse.error(message)));
    }

}