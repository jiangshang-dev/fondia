package org.jeecg.modules.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.media.mapper.VideoInfoMapper;
import org.jeecg.modules.media.entity.VideoInfoEntity;
import org.jeecg.modules.media.service.VideoInfoService;
import org.springframework.stereotype.Service;

/**
 * 视频信息服务实现类
 */
@Service
public class VideoInfoServiceImpl extends ServiceImpl<VideoInfoMapper, VideoInfoEntity> implements VideoInfoService {
    
    @Override
    public VideoInfoEntity getByVideoId(String videoId) {
        QueryWrapper<VideoInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId);
        return this.getOne(queryWrapper);
    }
    
    @Override
    public boolean updateStatus(String videoId, String status) {
        UpdateWrapper<VideoInfoEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("video_id", videoId);
        updateWrapper.set("status", status);
        return this.update(updateWrapper);
    }
    
    @Override
    public boolean createVideoInfo(VideoInfoEntity videoInfoEntity) {
        return this.save(videoInfoEntity);
    }
    
    @Override
    public boolean updateByVideoId(VideoInfoEntity videoInfoEntity) {
        UpdateWrapper<VideoInfoEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("video_id", videoInfoEntity.getVideoId());
        return this.update(videoInfoEntity, updateWrapper);
    }
}