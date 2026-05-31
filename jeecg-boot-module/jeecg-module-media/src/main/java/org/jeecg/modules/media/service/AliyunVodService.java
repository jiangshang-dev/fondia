package org.jeecg.modules.media.service;

import com.aliyuncs.vod.model.v20170321.*;

/**
 * 阿里云VOD服务接口
 */
public interface AliyunVodService {

    /**
     * 获取视频上传地址和凭证
     * @param title 视频标题
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return CreateUploadVideoResponse
     * @throws Exception 异常
     */
    CreateUploadVideoResponse createUploadVideo(String title, String fileName, Long fileSize) throws Exception;

    /**
     * 刷新视频上传凭证
     * @param videoId 视频ID
     * @return RefreshUploadVideoResponse
     * @throws Exception 异常
     */
    RefreshUploadVideoResponse refreshUploadVideo(String videoId) throws Exception;

    /**
     * 获取视频信息
     * @param videoId 视频ID
     * @return GetVideoInfoResponse
     * @throws Exception 异常
     */
    GetVideoInfoResponse getVideoInfo(String videoId) throws Exception;

    /**
     * 获取视频播放地址
     * @param videoId 视频ID
     * @return GetPlayInfoResponse
     * @throws Exception 异常
     */
    GetPlayInfoResponse getPlayInfo(String videoId) throws Exception;

    /**
     * 获取视频播放凭证
     * @param videoId 视频ID
     * @return GetVideoPlayAuthResponse
     * @throws Exception 异常
     */
    GetVideoPlayAuthResponse getVideoPlayAuth(String videoId) throws Exception;

    /**
     * 删除视频
     * @param videoId 视频ID
     * @return DeleteVideoResponse
     * @throws Exception 异常
     */
    DeleteVideoResponse deleteVideo(String videoId) throws Exception;

    /**
     * 批量删除视频
     * @param videoIds 视频ID列表
     * @return DeleteVideoResponse
     * @throws Exception 异常
     */
    DeleteVideoResponse deleteVideos(String videoIds) throws Exception;

    /**
     * 修改视频信息
     * @param videoId 视频ID
     * @param title 标题
     * @param description 描述
     * @param tags 标签
     * @return UpdateVideoInfoResponse
     * @throws Exception 异常
     */
    UpdateVideoInfoResponse updateVideoInfo(String videoId, String title, String description, String tags) throws Exception;
}
