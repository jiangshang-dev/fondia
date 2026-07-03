package org.jeecg.modules.support.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.support.entity.FdCsConfig;
import org.jeecg.modules.support.mapper.FdCsConfigMapper;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.vo.CsConfigVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class FdCsConfigServiceImpl extends ServiceImpl<FdCsConfigMapper, FdCsConfig> implements IFdCsConfigService {

    private static final long CONFIG_ID = 1L;

    @Resource
    private FdCsConfigMapper configMapper;

    @Override
    public FdCsConfig getConfigEntity() {
        FdCsConfig config = configMapper.selectById(CONFIG_ID);
        if (config == null) {
            config = new FdCsConfig()
                    .setId(CONFIG_ID)
                    .setWelcomeTitle("Hi，小厨粉")
                    .setWelcomeTag("智能自助")
                    .setWelcomeDesc("我是小厨专属客服，有什么可以帮您的？")
                    .setHumanGreeting("您好，我是人工客服小美，很高兴为您服务！请问有什么可以帮您的？")
                    .setPrivacyTip("小厨致力保护您的隐私，本次通话已加密。")
                    .setAiAppId("2072629675695808513")
                    .setTransferKeywords("人工,转人工,人工客服,召唤人工");
            configMapper.insert(config);
        }
        return config;
    }

    @Override
    public CsConfigVO getAppConfig() {
        FdCsConfig config = getConfigEntity();
        CsConfigVO vo = new CsConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }

    @Override
    public void saveOrUpdateConfig(FdCsConfig config) {
        config.setId(CONFIG_ID);
        config.setUpdateTime(System.currentTimeMillis());
        if (configMapper.selectById(CONFIG_ID) == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
    }
}
