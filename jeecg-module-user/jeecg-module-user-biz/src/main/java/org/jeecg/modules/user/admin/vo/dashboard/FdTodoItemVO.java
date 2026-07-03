package org.jeecg.modules.user.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "工作台待办")
public class FdTodoItemVO {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "跳转路由")
    private String route;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "颜色")
    private String color;
}
