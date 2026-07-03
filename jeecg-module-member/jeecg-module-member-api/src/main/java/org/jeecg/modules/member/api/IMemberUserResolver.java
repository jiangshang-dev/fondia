package org.jeecg.modules.member.api;

public interface IMemberUserResolver {
    Long resolveUserId(String authorization);
}
