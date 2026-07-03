package org.jeecg.modules.member.service.app.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.*;
import org.jeecg.modules.member.mapper.FdUserMemberSyncMapper;
import org.jeecg.modules.member.service.*;
import org.jeecg.modules.member.service.app.IMemberCheckinAppService;
import org.jeecg.modules.member.service.core.PointsCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberCheckinAppServiceImpl implements IMemberCheckinAppService {

    @Resource
    private IFdCheckinConfigService configService;
    @Resource
    private IFdCheckinRecordService recordService;
    @Resource
    private PointsCoreService pointsCoreService;
    @Resource
    private FdUserMemberSyncMapper userSyncMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> doCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<FdCheckinRecord> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(FdCheckinRecord::getUserId, userId).eq(FdCheckinRecord::getCheckinDate, today);
        if (recordService.count(existWrapper) > 0) {
            throw new JeecgBootException("今日已签到");
        }

        LocalDate yesterday = today.minusDays(1);
        LambdaQueryWrapper<FdCheckinRecord> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.eq(FdCheckinRecord::getUserId, userId).eq(FdCheckinRecord::getCheckinDate, yesterday);
        FdCheckinRecord yesterdayRecord = recordService.getOne(yesterdayWrapper, false);

        int streakDays = yesterdayRecord != null ? yesterdayRecord.getStreakDays() + 1 : 1;
        int cycleDayIndex = ((streakDays - 1) % 7) + 1;

        List<FdCheckinConfig> configs = configService.list(new LambdaQueryWrapper<FdCheckinConfig>()
                .eq(FdCheckinConfig::getStatus, 1)
                .eq(FdCheckinConfig::getDayIndex, cycleDayIndex));
        FdCheckinConfig config = configs.isEmpty() ? null : configs.get(0);
        int rewardPoints = config != null ? config.getRewardPoints() : 0;
        int rewardGrowth = config != null ? config.getRewardGrowth() : 0;

        long now = System.currentTimeMillis();
        FdCheckinRecord record = new FdCheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setStreakDays(streakDays);
        record.setCycleDayIndex(cycleDayIndex);
        record.setRewardPoints(rewardPoints);
        record.setRewardGrowth(rewardGrowth);
        record.setCreateTime(now);
        recordService.save(record);

        int balanceAfter = pointsCoreService.getBalance(userId);
        if (rewardPoints > 0) {
            balanceAfter = pointsCoreService.addPoints(userId, rewardPoints, "CHECKIN", String.valueOf(record.getId()), "每日签到");
        }

        userSyncMapper.syncAfterCheckin(userId, balanceAfter, streakDays, rewardGrowth, now);

        Map<String, Object> data = new HashMap<>();
        data.put("record", record);
        data.put("rewardPoints", rewardPoints);
        data.put("rewardGrowth", rewardGrowth);
        data.put("balanceAfter", balanceAfter);
        data.put("streakDays", streakDays);
        return data;
    }
}
