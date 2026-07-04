package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdAiQuotaConfig;
import org.jeecg.modules.member.mapper.FdAiQuotaConfigMapper;
import org.jeecg.modules.member.service.IFdAiQuotaConfigService;
import org.springframework.stereotype.Service;

@Service
public class FdAiQuotaConfigServiceImpl extends ServiceImpl<FdAiQuotaConfigMapper, FdAiQuotaConfig>
        implements IFdAiQuotaConfigService {

    @Override
    public FdAiQuotaConfig getByFeatureCode(String featureCode) {
        LambdaQueryWrapper<FdAiQuotaConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdAiQuotaConfig::getFeatureCode, featureCode)
                .eq(FdAiQuotaConfig::getStatus, 1)
                .last("LIMIT 1");
        return getOne(wrapper, false);
    }
}
