package org.jeecg.modules.member.service.app;

public interface IPointsTaskAppService {
    int completeTask(Long userId, String taskCode, String bizRef);
}
