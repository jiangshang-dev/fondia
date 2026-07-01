package org.jeecg.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "提交意见反馈")
public class FeedbackSubmitDTO {

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型")
    private String feedbackType;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 300, message = "反馈内容不能超过300字")
    @Schema(description = "反馈内容")
    private String content;

    @Size(max = 100, message = "联系方式不能超过100字")
    @Schema(description = "联系方式")
    private String contact;

    @Size(max = 3, message = "最多上传3张图片")
    @Schema(description = "图片URL列表")
    private List<String> images;
}
