package org.jeecg.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件上传结果")
public class UploadFileVO {

    @Schema(description = "相对路径")
    private String path;

    @Schema(description = "可访问地址")
    private String url;
}
