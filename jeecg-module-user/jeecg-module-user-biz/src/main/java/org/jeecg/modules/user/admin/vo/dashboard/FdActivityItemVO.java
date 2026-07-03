package org.jeecg.modules.user.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "工作台动态")
public class FdActivityItemVO {

    @Schema(description = "展示名称")
    private String name;

    @Schema(description = "描述(可含HTML)")
    private String desc;

    @Schema(description = "时间展示")
    private String date;

    @Schema(description = "时间戳(毫秒，排序用)")
    private Long time;

    @Schema(description = "头像图标")
    private String avatar;
}
