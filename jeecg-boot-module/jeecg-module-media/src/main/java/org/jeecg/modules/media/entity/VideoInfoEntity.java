package org.jeecg.modules.media.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 视频信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("video_info")
public class VideoInfoEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 阿里云视频ID
     */
    private String videoId;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 视频时长(秒)
     */
    private Integer duration;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 视频状态
     */
    private String status;

    /**
     * 视频大小(字节)
     */
    private Long size;

    /**
     * 分类ID
     */
    private Long cateId;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}