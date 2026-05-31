package org.jeecg.modules.media.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 阿里云VOD配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("vod_config")
public class VodConfigEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 阿里云AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 区域ID
     */
    private String regionId;

    /**
     * 接入点
     */
    private String endpoint;

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