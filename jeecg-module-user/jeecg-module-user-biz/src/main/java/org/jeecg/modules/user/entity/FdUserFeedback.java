package org.jeecg.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TableName("fd_user_feedback")
@Schema(description = "用户意见反馈")
public class FdUserFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "反馈类型")
    private String feedbackType;

    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "图片URL列表(JSON)")
    private String images;

    @Schema(description = "状态：0待处理 1已处理")
    private Integer status;

    @Schema(description = "创建时间")
    private Long createTime;
}
