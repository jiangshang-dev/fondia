package org.jeecg.modules.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.media.mapper.VideoConfigMapper;
import org.jeecg.modules.media.entity.VideoConfigEntity;
import org.jeecg.modules.media.service.VideoConfigService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 视频业务配置服务实现类
 */
@Service
public class VideoConfigServiceImpl extends ServiceImpl<VideoConfigMapper, VideoConfigEntity> implements VideoConfigService {
    
    @Override
    public VideoConfigEntity getEnabledConfig() {
        QueryWrapper<VideoConfigEntity> queryWrapper = new QueryWrapper<>();
        // queryWrapper.eq("enable", true);
        return this.getOne(queryWrapper);
    }
    
    @Override
    @Cacheable(value = "videoConfig", key = "'enabled'")
    public VideoConfigEntity getEnabledConfigWithCache() {
        return getEnabledConfig();
    }
}