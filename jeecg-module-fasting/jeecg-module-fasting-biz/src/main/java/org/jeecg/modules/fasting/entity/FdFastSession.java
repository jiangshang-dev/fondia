package org.jeecg.modules.fasting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("fd_fast_session")
@Schema(description = "断食进食会话")
public class FdFastSession implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String deviceId;
    private String planCode;
    private String phase;
    private Date startTime;
    private Date endTime;
    private Integer durationSec;
    private String clientUuid;
    private Date createTime;
}
