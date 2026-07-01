package org.jeecg.modules.interaction.api;

/**
 * 解析当前登录用户 ID（微服务 Feign / 单体本地实现）
 */
public interface IInteractionUserResolver {

    Long resolveUserId(String authorization);
}
