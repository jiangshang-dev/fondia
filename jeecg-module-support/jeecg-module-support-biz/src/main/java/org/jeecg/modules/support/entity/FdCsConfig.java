package org.jeecg.modules.support.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TableName("fd_cs_config")
@Schema(description = "客服全局配置")
public class FdCsConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private String welcomeTitle;
    private String welcomeTag;
    private String welcomeDesc;
    private String humanGreeting;
    private String privacyTip;
    private String aiAppId;
    private String transferKeywords;
    private Long updateTime;
}
