package org.jeecg.modules.media.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 视频业务配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("video_config")
public class VideoConfigEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 最大并发上传数
     */
    private Integer maxConcurrentUploads;

    /**
     * 默认转码模板
     */
    private String defaultTranscodeTemplate;

    /**
     * 默认水印配置
     */
    private String defaultWatermarkConfig;

    /**
     * 存储位置
     */
    private String storageLocation;

    /**
     * 是否启用
     */
    private Boolean enable;

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