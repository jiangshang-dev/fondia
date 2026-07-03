package org.jeecg.modules.member.service.core;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.jeecg.modules.member.mapper.FdUserMemberSyncMapper;
import org.jeecg.modules.member.service.IFdMemberSubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberSubscriptionGrantService {

    @Resource
    private IFdMemberSubscriptionService subscriptionService;
    @Resource
    private FdUserMemberSyncMapper userSyncMapper;

    @Transactional(rollbackFor = Exception.class)
    public FdMemberSubscription grantMemberDays(Long userId, int days, String planCode, String sourceChannel) {
        if (days <= 0) {
            throw new JeecgBootException("会员天数无效");
        }
        long now = System.currentTimeMillis();
        long dayMs = days * 86400000L;

        LambdaQueryWrapper<FdMemberSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberSubscription::getUserId, userId)
                .eq(FdMemberSubscription::getStatus, 1)
                .gt(FdMemberSubscription::getExpireTime, now)
                .orderByDesc(FdMemberSubscription::getExpireTime)
                .last("LIMIT 1");
        FdMemberSubscription active = subscriptionService.getOne(wrapper, false);

        long startTime = now;
        long expireTime = now + dayMs;
        if (active != null && active.getExpireTime() > now) {
            startTime = active.getStartTime();
            expireTime = active.getExpireTime() + dayMs;
            active.setExpireTime(expireTime);
            active.setPlanCode(planCode != null ? planCode : active.getPlanCode());
            active.setSourceChannel(sourceChannel);
            active.setUpdateTime(now);
            subscriptionService.updateById(active);
            userSyncMapper.syncMemberStatus(userId, 1, expireTime, active.getPlanCode(), now);
            return active;
        }

        FdMemberSubscription sub = new FdMemberSubscription();
        sub.setUserId(userId);
        sub.setPlanId(0L);
        sub.setPlanCode(planCode != null ? planCode : "MALL_VIRTUAL");
        sub.setStatus(1);
        sub.setStartTime(startTime);
        sub.setExpireTime(expireTime);
        sub.setAutoRenew(0);
        sub.setSourceChannel(sourceChannel);
        sub.setCreateTime(now);
        sub.setUpdateTime(now);
        subscriptionService.save(sub);
        userSyncMapper.syncMemberStatus(userId, 1, expireTime, sub.getPlanCode(), now);
        return sub;
    }
}
