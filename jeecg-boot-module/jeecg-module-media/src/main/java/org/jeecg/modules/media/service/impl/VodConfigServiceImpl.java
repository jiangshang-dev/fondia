package org.jeecg.modules.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.media.mapper.VodConfigMapper;
import org.jeecg.modules.media.entity.VodConfigEntity;
import org.jeecg.modules.media.service.VodConfigService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 阿里云VOD配置服务实现类
 */
@Service
public class VodConfigServiceImpl extends ServiceImpl<VodConfigMapper, VodConfigEntity> implements VodConfigService {
    
    @Override
    public VodConfigEntity getEnabledConfig() {
        QueryWrapper<VodConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enable", true);
        return this.getOne(queryWrapper);
    }
    
    @Override
    @Cacheable(value = "vodConfig", key = "'enabled'")
    public VodConfigEntity getEnabledConfigWithCache() {
        return getEnabledConfig();
    }
}