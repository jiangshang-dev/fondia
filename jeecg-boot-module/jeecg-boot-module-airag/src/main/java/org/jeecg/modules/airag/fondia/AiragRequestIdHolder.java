package org.jeecg.modules.airag.fondia;

/**
 * 同进程调用 Airag 时传递 requestId，便于消费 SSE 历史事件。
 */
public final class AiragRequestIdHolder {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private AiragRequestIdHolder() {
    }

    public static void set(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String get() {
        return REQUEST_ID.get();
    }

    public static void clear() {
        REQUEST_ID.remove();
    }
}
