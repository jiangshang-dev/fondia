package org.jeecg.modules.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.member.entity.FdAiQuotaUsage;

public interface IFdAiQuotaUsageService extends IService<FdAiQuotaUsage> {

    int getUsedCount(Long userId, String featureCode, int usageDate);

    void ensureUsageRow(Long userId, String featureCode, int usageDate);

    int incrementIfUnderLimit(Long userId, String featureCode, int usageDate, int limit);

    void incrementUnlimited(Long userId, String featureCode, int usageDate);
}
