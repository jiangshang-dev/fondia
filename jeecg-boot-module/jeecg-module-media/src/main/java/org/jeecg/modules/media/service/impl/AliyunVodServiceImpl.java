package org.jeecg.modules.media.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.*;
import jakarta.annotation.PostConstruct;
import org.jeecg.modules.media.entity.VodConfigEntity;
import org.jeecg.modules.media.service.AliyunVodService;
import org.jeecg.modules.media.service.VodConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

/**
 * 阿里云VOD服务实现类
 */
@Service
public class AliyunVodServiceImpl implements AliyunVodService {
    
    @Autowired
    private VodConfigService vodConfigService;
    
    private IAcsClient client;
    
    // 用于控制并发上传的信号量
    private Semaphore uploadSemaphore;
    
    @PostConstruct
    public void init() {
        // 初始化阿里云客户端
        refreshClient();
        
        // 初始化信号量，默认100个并发
        uploadSemaphore = new Semaphore(100);
    }
    
    /**
     * 刷新阿里云客户端配置
     */
    public void refreshClient() {
        try {
            VodConfigEntity config = vodConfigService.getEnabledConfigWithCache();
            if (config != null) {
                DefaultProfile profile = DefaultProfile.getProfile(
                    config.getRegionId(), 
                    config.getAccessKeyId(), 
                    config.getAccessKeySecret()
                );
                client = new DefaultAcsClient(profile);
                
                // 更新信号量
                updateSemaphore(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 更新信号量
     * @param config 配置信息
     */
    private void updateSemaphore(VodConfigEntity config) {
        // 这里可以根据配置动态调整并发数
        // 为了简化，我们使用默认值
    }
    
    @Override
    public CreateUploadVideoResponse createUploadVideo(String title, String fileName, Long fileSize) throws Exception {
        // 获取信号量
        uploadSemaphore.acquire();
        
        try {
            CreateUploadVideoRequest request = new CreateUploadVideoRequest();
            request.setTitle(title);
            request.setFileName(fileName);
            if (fileSize != null) {
                request.setFileSize(fileSize);
            }
            
            return client.getAcsResponse(request);
        } finally {
            // 释放信号量
            uploadSemaphore.release();
        }
    }
    
    @Override
    public RefreshUploadVideoResponse refreshUploadVideo(String videoId) throws Exception {
        RefreshUploadVideoRequest request = new RefreshUploadVideoRequest();
        request.setVideoId(videoId);
        return client.getAcsResponse(request);
    }
    
    @Override
    public GetVideoInfoResponse getVideoInfo(String videoId) throws Exception {
        GetVideoInfoRequest request = new GetVideoInfoRequest();
        request.setVideoId(videoId);
        return client.getAcsResponse(request);
    }
    
    @Override
    public GetPlayInfoResponse getPlayInfo(String videoId) throws Exception {
        GetPlayInfoRequest request = new GetPlayInfoRequest();
        request.setVideoId(videoId);
        return client.getAcsResponse(request);
    }
    
    @Override
    public GetVideoPlayAuthResponse getVideoPlayAuth(String videoId) throws Exception {
        GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
        request.setVideoId(videoId);
        return client.getAcsResponse(request);
    }
    
    @Override
    public DeleteVideoResponse deleteVideo(String videoId) throws Exception {
        DeleteVideoRequest request = new DeleteVideoRequest();
        request.setVideoIds(videoId);
        return client.getAcsResponse(request);
    }
    
    @Override
    public DeleteVideoResponse deleteVideos(String videoIds) throws Exception {
        DeleteVideoRequest request = new DeleteVideoRequest();
        request.setVideoIds(videoIds);
        return client.getAcsResponse(request);
    }
    
    @Override
    public UpdateVideoInfoResponse updateVideoInfo(String videoId, String title, String description, String tags) throws Exception {
        UpdateVideoInfoRequest request = new UpdateVideoInfoRequest();
        request.setVideoId(videoId);
        if (title != null) {
            request.setTitle(title);
        }
        if (description != null) {
            request.setDescription(description);
        }
        if (tags != null) {
            request.setTags(tags);
        }
        return client.getAcsResponse(request);
    }
}