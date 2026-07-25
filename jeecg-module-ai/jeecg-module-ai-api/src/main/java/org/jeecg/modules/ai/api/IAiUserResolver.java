package org.jeecg.modules.ai.api;

/**
 * 解析当前登录用户 ID。
 */
@FunctionalInterface
public interface IAiUserResolver {

    Long resolveUserId(String authorization);
}
