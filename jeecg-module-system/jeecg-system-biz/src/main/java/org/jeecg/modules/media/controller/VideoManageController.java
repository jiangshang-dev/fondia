package org.jeecg.modules.media.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.media.entity.VideoInfoEntity;
import org.jeecg.modules.media.service.AliyunVodService;
import org.jeecg.modules.media.service.VideoInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频管理控制器
 */
@RestController
@RequestMapping("/sys/vod/video")
public class VideoManageController {
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private AliyunVodService aliyunVodService;
    
    /**
     * 分页查询视频列表
     * @param page 页码
     * @param limit 每页数量
     * @param title 视频标题（可选）
     * @param status 视频状态（可选）
     * @return 视频列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getVideoList(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer limit, @RequestParam(required = false) String title, @RequestParam(required = false) String status) {
        
        try {
            Page<VideoInfoEntity> pageParam = new Page<>(page, limit);
            QueryWrapper<VideoInfoEntity> queryWrapper = new QueryWrapper<>();
            
            if (title != null && !title.isEmpty()) {
                queryWrapper.like("title", title);
            }
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }
            
            queryWrapper.orderByDesc("create_time");
            
            Page<VideoInfoEntity> videoPage = videoInfoService.page(pageParam, queryWrapper);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", videoPage.getRecords());
            result.put("total", videoPage.getTotal());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取视频详情
     * @param id 视频ID
     * @return 视频详情
     */
    @GetMapping("/{id}")
    public Result<VideoInfoEntity> getVideoDetail(@PathVariable Long id) {
        try {
            VideoInfoEntity video = videoInfoService.getById(id);
            if (video != null) {
                return Result.ok(video);
            } else {
                return Result.error("视频不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新视频状态
     * @param videoId 阿里云视频ID
     * @param status 状态
     * @return 更新结果
     */
    @PostMapping("/status")
    public Result<String> updateVideoStatus(@RequestParam String videoId, @RequestParam String status) {
        
        try {
            boolean success = videoInfoService.updateStatus(videoId, status);
            if (success) {
                return Result.ok("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除视频 - 通过路径参数
     * @param id 数据库ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteVideo(@PathVariable Long id) {
        try {
            // 先根据数据库ID获取视频信息
            VideoInfoEntity video = videoInfoService.getById(id);
            if (video == null) {
                return Result.error("视频不存在");
            }
            
            // 调用阿里云接口删除视频
            try {
                aliyunVodService.deleteVideo(video.getVideoId());
            } catch (Exception e) {
                // 阿里云删除失败时记录日志，但继续删除本地记录
                e.printStackTrace();
            }
            
            // 删除数据库记录
            boolean success = videoInfoService.removeById(id);
            if (success) {
                return Result.ok("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除视频 - 通过阿里云视频ID
     * @param videoId 阿里云视频ID
     * @return 删除结果
     */
    @DeleteMapping("")
    public Result<String> deleteVideoByVideoId(@RequestParam String videoId) {
        try {
            // 先根据阿里云视频ID获取视频信息
            VideoInfoEntity video = videoInfoService.getByVideoId(videoId);
            if (video == null) {
                return Result.error("视频不存在");
            }
            
            // 调用阿里云接口删除视频
            try {
                aliyunVodService.deleteVideo(videoId);
            } catch (Exception e) {
                // 阿里云删除失败时记录日志，但继续删除本地记录
                e.printStackTrace();
            }
            
            // 删除数据库记录
            boolean success = videoInfoService.removeById(video.getId());
            if (success) {
                return Result.ok("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}