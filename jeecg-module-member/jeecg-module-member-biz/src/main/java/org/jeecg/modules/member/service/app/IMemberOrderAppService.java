package org.jeecg.modules.member.service.app;

import org.jeecg.modules.member.entity.FdMemberOrder;

public interface IMemberOrderAppService {
    FdMemberOrder createOrder(Long userId, Long planId, String payChannel, String clientPlatform);
}
