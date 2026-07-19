package org.jeecg.modules.airag.fondia;

import org.jeecg.common.util.oConvertUtils;

/**
 * 单体模式下 Fondia App 用户会话隔离（替代 HTTP Header X-Airag-Cache-User）。
 */
public final class AiragCacheUserHolder {

    private static final ThreadLocal<String> CACHE_USERNAME = new ThreadLocal<>();

    private AiragCacheUserHolder() {
    }

    public static void set(String cacheUsername) {
        if (oConvertUtils.isNotEmpty(cacheUsername)) {
            CACHE_USERNAME.set(cacheUsername);
        }
    }

    public static String get() {
        return CACHE_USERNAME.get();
    }

    public static void clear() {
        CACHE_USERNAME.remove();
    }
}
