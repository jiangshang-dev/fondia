package org.jeecg.modules.media.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.media.entity.VodConfigEntity;
import org.jeecg.modules.media.entity.VideoConfigEntity;
import org.jeecg.modules.media.service.VodConfigService;
import org.jeecg.modules.media.service.VideoConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * VOD配置管理控制器
 */
@RestController
@RequestMapping("/sys/vod/config")
public class VodConfigController {
    
    @Resource
    private VodConfigService vodConfigService;
    
    @Resource
    private VideoConfigService videoConfigService;
    
    /**
     * 获取阿里云VOD配置
     * @return 配置信息
     */
    @GetMapping("/aliyun")
    public Result<VodConfigEntity> getVodConfig() {
        try {
            VodConfigEntity config = vodConfigService.getEnabledConfig();
            return Result.ok(config);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 保存阿里云VOD配置
     * @param config 配置信息
     * @return 保存结果
     */
    @PostMapping("/aliyun")
    public Result<VodConfigEntity> saveVodConfig(@RequestBody VodConfigEntity config) {
        try {
            // 如果是启用状态，先禁用其他配置
            if (config.getEnable() != null && config.getEnable()) {
                VodConfigEntity enabledConfig = vodConfigService.getEnabledConfig();
                if (enabledConfig != null && !enabledConfig.getId().equals(config.getId())) {
                    enabledConfig.setEnable(false);
                    vodConfigService.updateById(enabledConfig);
                }
            }
            
            if (config.getId() == null) {
                vodConfigService.save(config);
            } else {
                vodConfigService.updateById(config);
            }

            return Result.ok(config);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取视频业务配置
     * @return 配置信息
     */
    @GetMapping("/video")
    public Result<VodConfigEntity> getVideoConfig() {
        try {
            VodConfigEntity config = vodConfigService.getEnabledConfig();
            return Result.ok(config);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 保存视频业务配置
     * @param config 配置信息
     * @return 保存结果
     */
    @PostMapping("/video")
    public Result<VideoConfigEntity> saveVideoConfig(@RequestBody VideoConfigEntity config) {
        try {
            // 如果是启用状态，先禁用其他配置
            if (config.getEnable() != null && config.getEnable()) {
                VideoConfigEntity enabledConfig = videoConfigService.getEnabledConfig();
                if (enabledConfig != null && !enabledConfig.getId().equals(config.getId())) {
                    enabledConfig.setEnable(false);
                    videoConfigService.updateById(enabledConfig);
                }
            }
            
            if (config.getId() == null) {
                videoConfigService.save(config);
            } else {
                videoConfigService.updateById(config);
            }

            return Result.ok(config);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}