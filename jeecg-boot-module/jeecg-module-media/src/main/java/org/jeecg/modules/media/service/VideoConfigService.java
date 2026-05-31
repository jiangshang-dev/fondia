package org.jeecg.modules.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.media.entity.VideoConfigEntity;

/**
 * 视频业务配置服务接口
 */
public interface VideoConfigService extends IService<VideoConfigEntity> {
    /**
     * 获取启用的视频配置
     * @return VideoConfigEntity
     */
    VideoConfigEntity getEnabledConfig();

    /**
     * 获取启用的视频配置（带缓存）
     * @return VideoConfigEntity
     */
    VideoConfigEntity getEnabledConfigWithCache();
}