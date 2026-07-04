package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.member.entity.FdAiQuotaUsage;
import org.jeecg.modules.member.mapper.FdAiQuotaUsageMapper;
import org.jeecg.modules.member.service.IFdAiQuotaUsageService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FdAiQuotaUsageServiceImpl extends ServiceImpl<FdAiQuotaUsageMapper, FdAiQuotaUsage>
        implements IFdAiQuotaUsageService {

    @Resource
    private FdAiQuotaUsageMapper usageMapper;

    @Override
    public int getUsedCount(Long userId, String featureCode, int usageDate) {
        LambdaQueryWrapper<FdAiQuotaUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdAiQuotaUsage::getUserId, userId)
                .eq(FdAiQuotaUsage::getFeatureCode, featureCode)
                .eq(FdAiQuotaUsage::getUsageDate, usageDate);
        FdAiQuotaUsage usage = getOne(wrapper, false);
        return usage == null || usage.getUsedCount() == null ? 0 : usage.getUsedCount();
    }

    @Override
    public void ensureUsageRow(Long userId, String featureCode, int usageDate) {
        LambdaQueryWrapper<FdAiQuotaUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdAiQuotaUsage::getUserId, userId)
                .eq(FdAiQuotaUsage::getFeatureCode, featureCode)
                .eq(FdAiQuotaUsage::getUsageDate, usageDate);
        if (getOne(wrapper, false) != null) {
            return;
        }
        Date now = new Date();
        FdAiQuotaUsage row = new FdAiQuotaUsage();
        row.setUserId(userId);
        row.setFeatureCode(featureCode);
        row.setUsageDate(usageDate);
        row.setUsedCount(0);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        save(row);
    }

    @Override
    public int incrementIfUnderLimit(Long userId, String featureCode, int usageDate, int limit) {
        ensureUsageRow(userId, featureCode, usageDate);
        return usageMapper.incrementIfUnderLimit(userId, featureCode, usageDate, limit);
    }

    @Override
    public void incrementUnlimited(Long userId, String featureCode, int usageDate) {
        ensureUsageRow(userId, featureCode, usageDate);
        LambdaQueryWrapper<FdAiQuotaUsage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdAiQuotaUsage::getUserId, userId)
                .eq(FdAiQuotaUsage::getFeatureCode, featureCode)
                .eq(FdAiQuotaUsage::getUsageDate, usageDate);
        FdAiQuotaUsage usage = getOne(wrapper, false);
        if (usage == null) {
            return;
        }
        usage.setUsedCount((usage.getUsedCount() == null ? 0 : usage.getUsedCount()) + 1);
        usage.setUpdateTime(new Date());
        updateById(usage);
    }
}
