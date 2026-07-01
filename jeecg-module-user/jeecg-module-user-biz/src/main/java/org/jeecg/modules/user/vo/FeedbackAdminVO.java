package org.jeecg.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "意见反馈管理列表项")
public class FeedbackAdminVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userPhone;
    private String feedbackType;
    private String content;
    private String contact;
    private List<String> images;
    private Integer status;
    private Long createTime;
}
