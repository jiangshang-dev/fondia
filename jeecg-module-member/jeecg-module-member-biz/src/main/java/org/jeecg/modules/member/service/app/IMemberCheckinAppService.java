package org.jeecg.modules.member.service.app;

import java.util.Map;

public interface IMemberCheckinAppService {
    Map<String, Object> doCheckin(Long userId);
}
