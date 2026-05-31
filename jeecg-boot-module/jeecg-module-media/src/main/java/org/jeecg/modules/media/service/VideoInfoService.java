package org.jeecg.modules.media.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.media.entity.VideoInfoEntity;

/**
 * 视频信息服务接口
 */
public interface VideoInfoService extends IService<VideoInfoEntity> {
    /**
     * 根据阿里云视频ID获取视频信息
     * @param videoId 阿里云视频ID
     * @return VideoInfoEntity
     */
    VideoInfoEntity getByVideoId(String videoId);

    /**
     * 更新视频状态
     * @param videoId 阿里云视频ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(String videoId, String status);

    /**
     * 创建视频信息
     * @param videoInfoEntity 视频信息实体
     * @return 是否创建成功
     */
    boolean createVideoInfo(VideoInfoEntity videoInfoEntity);

    /**
     * 根据阿里云视频ID更新视频信息
     * @param videoInfoEntity 视频信息实体
     * @return 是否更新成功
     */
    boolean updateByVideoId(VideoInfoEntity videoInfoEntity);
}