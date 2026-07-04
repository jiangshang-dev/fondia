package org.jeecg.modules.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.member.entity.FdAiQuotaConfig;

public interface IFdAiQuotaConfigService extends IService<FdAiQuotaConfig> {

    FdAiQuotaConfig getByFeatureCode(String featureCode);
}
