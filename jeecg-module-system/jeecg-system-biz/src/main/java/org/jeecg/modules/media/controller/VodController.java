package org.jeecg.modules.media.controller;

import com.aliyuncs.vod.model.v20170321.*;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.media.entity.VideoInfoEntity;
import org.jeecg.modules.media.service.AliyunVodService;
import org.jeecg.modules.media.service.VideoInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频点播控制器
 */
@RestController
@RequestMapping("/sys/vod/auth")
public class VodController {
    @Resource
    private AliyunVodService aliyunVodService;
    @Resource
    private VideoInfoService videoInfoService;
    
    /**
     * 获取视频上传地址和凭证
     * @param title 视频标题
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return 上传地址和凭证
     */
    @PostMapping("/upload/auth")
    public Result<Map<String, Object>> createUploadVideo(@RequestParam(required = false) String title, @RequestParam(required = false) String fileName, @RequestParam(required = false) Long fileSize) {

        try {
            // 如果通过表单参数没有获取到值，尝试从请求体获取
            if (title == null || fileName == null) {
                return Result.error("缺少必需参数");
            }

            CreateUploadVideoResponse response = aliyunVodService.createUploadVideo(title, fileName, fileSize);

            Map<String, Object> result = new HashMap<>();
            result.put("videoId", response.getVideoId());
            result.put("uploadAddress", response.getUploadAddress());
            result.put("uploadAuth", response.getUploadAuth());

            // 保存视频信息到数据库
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setVideoId(response.getVideoId());
            videoInfo.setTitle(title);
            videoInfo.setStatus("Uploading");
            videoInfo.setCreateTime(new Date());
            videoInfo.setUpdateTime(new Date());
            if (fileSize != null) {
                videoInfo.setSize(fileSize);
            }
            videoInfoService.createVideoInfo(videoInfo);

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取视频上传地址和凭证 (支持JSON格式请求体)
     * @param request 请求参数
     * @return 上传地址和凭证
     */
    @PostMapping("/upload/auth/json")
    public Result<Map<String, Object>> createUploadVideoJson(@RequestBody UploadAuthRequest request) {
        try {
            if (request.getTitle() == null || request.getFileName() == null) {
                return Result.error("缺少必需参数");
            }

            // 检查文件大小是否合理（不超过1GB）
            if (request.getFileSize() != null && request.getFileSize() > 1024L * 1024L * 1024L) {
                return Result.error("文件大小不能超过1GB");
            }

            CreateUploadVideoResponse response = aliyunVodService.createUploadVideo(
                request.getTitle(), 
                request.getFileName(), 
                request.getFileSize()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("videoId", response.getVideoId());
            result.put("uploadAddress", response.getUploadAddress());
            result.put("uploadAuth", response.getUploadAuth());
            result.put("requestId", response.getRequestId());

            // 保存视频信息到数据库
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setVideoId(response.getVideoId());
            videoInfo.setTitle(request.getTitle());
            videoInfo.setStatus("Uploading");
            if (request.getFileSize() != null) {
                videoInfo.setSize(request.getFileSize());
            }
            videoInfoService.createVideoInfo(videoInfo);

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传凭证获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新视频上传凭证
     * @param videoId 视频ID
     * @return 新的上传凭证
     */
    @PostMapping("/upload/refresh")
    public Result<Map<String, Object>> refreshUploadVideo(@RequestParam String videoId) {
        try {
            RefreshUploadVideoResponse response = aliyunVodService.refreshUploadVideo(videoId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("uploadAddress", response.getUploadAddress());
            result.put("uploadAuth", response.getUploadAuth());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取视频播放地址
     * @param videoId 视频ID
     * @return 播放地址
     */
    @GetMapping("/play/info")
    public Result<Map<String, Object>> getPlayInfo(@RequestParam(value = "videoId") String videoId) {
        try {
            GetPlayInfoResponse response = aliyunVodService.getPlayInfo(videoId);

            List<GetPlayInfoResponse.PlayInfo> playInfoList = response.getPlayInfoList();
            Map<String, Object> result = new HashMap<>();
            result.put("videoBase", response.getVideoBase());

            if (!playInfoList.isEmpty()) {
                result.put("playUrl", playInfoList.getFirst().getPlayURL());
            }

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取视频播放凭证
     * @param videoId 视频ID
     * @return 播放凭证
     */
    @GetMapping("/play/auth")
    public Result<Map<String, Object>> getVideoPlayAuth(@RequestParam String videoId) {
        try {
            GetVideoPlayAuthResponse response = aliyunVodService.getVideoPlayAuth(videoId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("playAuth", response.getPlayAuth());
            result.put("videoMeta", response.getVideoMeta());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除视频
     * @param videoId 视频ID
     * @return 删除结果
     */
    @DeleteMapping("/video")
    public Result<Map<String, Object>> deleteVideo(@RequestParam String videoId) {
        try {
            VideoInfoEntity videoInfo = videoInfoService.getById(videoId);
            String videoId1 = videoInfo.getVideoId();
            boolean isSuccess = videoInfoService.removeById(videoId);

            Map<String, Object> result = new HashMap<>();
            if (isSuccess) {
                DeleteVideoResponse response = aliyunVodService.deleteVideo(videoId1);
                result.put("requestId", response.getRequestId());
            }
            result.put("message", "删除成功");
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新视频信息
     * @param videoId 视频ID
     * @param title 标题
     * @param description 描述
     * @param tags 标签
     * @return 更新结果
     */
    @PutMapping("/video")
    public Result<Map<String, Object>> updateVideoInfo(
            @RequestParam String videoId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tags) {
        
        try {
            UpdateVideoInfoResponse response = aliyunVodService.updateVideoInfo(videoId, title, description, tags);
            
            // 更新数据库中的视频信息
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setVideoId(videoId);
            if (title != null) {
                videoInfo.setTitle(title);
            }
            if (description != null) {
                videoInfo.setDescription(description);
            }
            if (tags != null) {
                videoInfo.setTags(tags);
            }
            videoInfoService.updateByVideoId(videoInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "更新成功");

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建一个内部类用于接收JSON数据
     */
    public static class UploadAuthRequest {
        private String title;
        private String fileName;
        private Long fileSize;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }
    }

}