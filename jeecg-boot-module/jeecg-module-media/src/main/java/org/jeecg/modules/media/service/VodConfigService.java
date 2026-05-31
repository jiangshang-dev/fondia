package org.jeecg.modules.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.media.entity.VodConfigEntity;

/**
 * 阿里云VOD配置服务接口
 */
public interface VodConfigService extends IService<VodConfigEntity> {
    /**
     * 获取启用的VOD配置
     * @return VodConfigEntity
     */
    VodConfigEntity getEnabledConfig();

    /**
     * 获取启用的VOD配置（带缓存）
     * @return VodConfigEntity
     */
    VodConfigEntity getEnabledConfigWithCache();
}