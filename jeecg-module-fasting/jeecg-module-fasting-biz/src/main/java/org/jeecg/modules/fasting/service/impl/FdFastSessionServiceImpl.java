package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.fasting.dto.FastSessionEndDTO;
import org.jeecg.modules.fasting.dto.FastSessionStartDTO;
import org.jeecg.modules.fasting.dto.FastSessionSyncDTO;
import org.jeecg.modules.fasting.dto.FastSessionSyncItemDTO;
import org.jeecg.modules.fasting.entity.FdFastPlan;
import org.jeecg.modules.fasting.entity.FdFastSession;
import org.jeecg.modules.fasting.mapper.FdFastSessionMapper;
import org.jeecg.modules.fasting.service.IFdFastPlanService;
import org.jeecg.modules.fasting.service.IFdFastSessionService;
import org.jeecg.modules.fasting.vo.FastDailyStatVO;
import org.jeecg.modules.fasting.vo.FastSessionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FdFastSessionServiceImpl extends ServiceImpl<FdFastSessionMapper, FdFastSession>
        implements IFdFastSessionService {

    private static final SimpleDateFormat DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private IFdFastPlanService planService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FastSessionVO startSession(Long userId, String deviceId, FastSessionStartDTO dto) {
        closeOpenSession(userId, deviceId, new Date());
        FdFastSession session = new FdFastSession();
        session.setUserId(userId);
        session.setDeviceId(deviceId);
        session.setPlanCode(dto.getPlanCode());
        session.setPhase(dto.getPhase());
        session.setStartTime(parseDateTime(dto.getStartTime(), new Date()));
        session.setClientUuid(oConvertUtils.isNotEmpty(dto.getClientUuid())
                ? dto.getClientUuid()
                : UUID.randomUUID().toString().replace("-", ""));
        session.setCreateTime(new Date());
        save(session);
        return toVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FastSessionVO endSession(Long userId, String deviceId, FastSessionEndDTO dto) {
        FdFastSession session = findSession(userId, deviceId, dto.getSessionId(), dto.getClientUuid());
        if (session == null) {
            throw new JeecgBootException("未找到进行中的会话");
        }
        Date endTime = parseDateTime(dto.getEndTime(), new Date());
        session.setEndTime(endTime);
        if (session.getStartTime() != null) {
            long sec = (endTime.getTime() - session.getStartTime().getTime()) / 1000;
            session.setDurationSec((int) Math.max(sec, 0));
        }
        updateById(session);
        return toVO(session);
    }

    @Override
    public FastSessionVO getCurrentSession(Long userId, String deviceId) {
        FdFastSession session = findOpenSession(userId, deviceId);
        return session == null ? null : toVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncSessions(Long userId, String deviceId, FastSessionSyncDTO dto) {
        if (dto == null || dto.getItems() == null) {
            return;
        }
        for (FastSessionSyncItemDTO item : dto.getItems()) {
            if (oConvertUtils.isEmpty(item.getClientUuid())) {
                continue;
            }
            long count = count(new LambdaQueryWrapper<FdFastSession>()
                    .eq(FdFastSession::getClientUuid, item.getClientUuid()));
            if (count > 0) {
                continue;
            }
            FdFastSession session = new FdFastSession();
            session.setUserId(userId);
            session.setDeviceId(deviceId);
            session.setPlanCode(item.getPlanCode());
            session.setPhase(item.getPhase());
            session.setStartTime(parseDateTime(item.getStartTime(), new Date()));
            session.setEndTime(parseDateTime(item.getEndTime(), null));
            session.setDurationSec(item.getDurationSec());
            session.setClientUuid(item.getClientUuid());
            session.setCreateTime(new Date());
            save(session);
        }
    }

    @Override
    public List<FastDailyStatVO> getWeeklyStats(Long userId, String deviceId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        Date from = truncateDate(cal.getTime());
        LambdaQueryWrapper<FdFastSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FdFastSession::getStartTime, from);
        if (userId != null) {
            wrapper.eq(FdFastSession::getUserId, userId);
        } else if (oConvertUtils.isNotEmpty(deviceId)) {
            wrapper.eq(FdFastSession::getDeviceId, deviceId);
        }
        wrapper.eq(FdFastSession::getPhase, "FASTING");
        wrapper.isNotNull(FdFastSession::getEndTime);
        List<FdFastSession> sessions = list(wrapper);

        Map<String, Integer> actualMap = new HashMap<>();
        for (FdFastSession session : sessions) {
            String day = DATE.format(session.getStartTime());
            int sec = session.getDurationSec() != null ? session.getDurationSec() : 0;
            actualMap.merge(day, sec, Integer::sum);
        }

        FdFastPlan defaultPlan = planService.getOne(new LambdaQueryWrapper<FdFastPlan>()
                .eq(FdFastPlan::getCode, "16_8")
                .last("LIMIT 1"));
        int targetSec = defaultPlan != null ? defaultPlan.getFastHours() * 3600 : 16 * 3600;

        List<FastDailyStatVO> result = new ArrayList<>();
        cal = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -i);
            String dayStr = DATE.format(dayCal.getTime());
            int actual = actualMap.getOrDefault(dayStr, 0);
            FastDailyStatVO vo = new FastDailyStatVO();
            vo.setStatDate(dayStr);
            vo.setTargetFastSec(targetSec);
            vo.setActualFastSec(actual);
            vo.setCompleted(actual >= targetSec ? 1 : 0);
            result.add(vo);
        }
        return result;
    }

    private void closeOpenSession(Long userId, String deviceId, Date endTime) {
        FdFastSession open = findOpenSession(userId, deviceId);
        if (open != null) {
            open.setEndTime(endTime);
            if (open.getStartTime() != null) {
                long sec = (endTime.getTime() - open.getStartTime().getTime()) / 1000;
                open.setDurationSec((int) Math.max(sec, 0));
            }
            updateById(open);
        }
    }

    private FdFastSession findOpenSession(Long userId, String deviceId) {
        LambdaQueryWrapper<FdFastSession> wrapper = buildOwnerWrapper(userId, deviceId);
        wrapper.isNull(FdFastSession::getEndTime);
        wrapper.orderByDesc(FdFastSession::getStartTime);
        wrapper.last("LIMIT 1");
        return getOne(wrapper);
    }

    private FdFastSession findSession(Long userId, String deviceId, Long sessionId, String clientUuid) {
        if (sessionId != null) {
            return getById(sessionId);
        }
        if (oConvertUtils.isNotEmpty(clientUuid)) {
            return getOne(new LambdaQueryWrapper<FdFastSession>()
                    .eq(FdFastSession::getClientUuid, clientUuid)
                    .last("LIMIT 1"));
        }
        return findOpenSession(userId, deviceId);
    }

    private LambdaQueryWrapper<FdFastSession> buildOwnerWrapper(Long userId, String deviceId) {
        LambdaQueryWrapper<FdFastSession> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(FdFastSession::getUserId, userId);
        } else if (oConvertUtils.isNotEmpty(deviceId)) {
            wrapper.eq(FdFastSession::getDeviceId, deviceId);
        }
        return wrapper;
    }

    private Date parseDateTime(String text, Date defaultValue) {
        if (oConvertUtils.isEmpty(text)) {
            return defaultValue;
        }
        try {
            return DATETIME.parse(text);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    private Date truncateDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private FastSessionVO toVO(FdFastSession session) {
        FastSessionVO vo = new FastSessionVO();
        vo.setId(session.getId());
        vo.setPlanCode(session.getPlanCode());
        vo.setPhase(session.getPhase());
        vo.setStartTime(session.getStartTime() != null ? DATETIME.format(session.getStartTime()) : null);
        vo.setEndTime(session.getEndTime() != null ? DATETIME.format(session.getEndTime()) : null);
        vo.setDurationSec(session.getDurationSec());
        vo.setClientUuid(session.getClientUuid());
        return vo;
    }
}
