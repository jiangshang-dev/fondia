package org.jeecg.modules.fasting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fasting.dto.FastSessionEndDTO;
import org.jeecg.modules.fasting.dto.FastSessionStartDTO;
import org.jeecg.modules.fasting.dto.FastSessionSyncDTO;
import org.jeecg.modules.fasting.entity.FdFastSession;
import org.jeecg.modules.fasting.vo.FastDailyStatVO;
import org.jeecg.modules.fasting.vo.FastSessionVO;

import java.util.List;

public interface IFdFastSessionService extends IService<FdFastSession> {
    FastSessionVO startSession(Long userId, String deviceId, FastSessionStartDTO dto);

    FastSessionVO endSession(Long userId, String deviceId, FastSessionEndDTO dto);

    FastSessionVO getCurrentSession(Long userId, String deviceId);

    void syncSessions(Long userId, String deviceId, FastSessionSyncDTO dto);

    List<FastDailyStatVO> getWeeklyStats(Long userId, String deviceId);
}
