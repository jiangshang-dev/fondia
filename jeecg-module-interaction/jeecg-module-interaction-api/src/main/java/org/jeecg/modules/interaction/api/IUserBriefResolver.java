package org.jeecg.modules.interaction.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析评论展示所需的用户昵称、头像
 */
public interface IUserBriefResolver {

    InteractionUserBrief getBrief(Long userId);

    default Map<Long, InteractionUserBrief> batchGet(Collection<Long> userIds) {
        Map<Long, InteractionUserBrief> map = new HashMap<>();
        if (userIds == null) {
            return map;
        }
        for (Long userId : userIds) {
            if (userId == null || userId <= 0) {
                continue;
            }
            InteractionUserBrief brief = getBrief(userId);
            if (brief != null) {
                map.put(userId, brief);
            }
        }
        return map;
    }
}
