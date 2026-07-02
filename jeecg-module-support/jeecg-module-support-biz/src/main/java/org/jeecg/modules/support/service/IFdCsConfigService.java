package org.jeecg.modules.support.service;

import org.jeecg.modules.support.entity.FdCsConfig;
import org.jeecg.modules.support.vo.CsConfigVO;

public interface IFdCsConfigService {

    FdCsConfig getConfigEntity();

    CsConfigVO getAppConfig();

    void saveOrUpdateConfig(FdCsConfig config);
}
