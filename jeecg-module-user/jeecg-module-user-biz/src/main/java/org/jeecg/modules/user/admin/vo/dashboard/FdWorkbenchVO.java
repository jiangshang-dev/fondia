package org.jeecg.modules.user.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "工作台数据")
public class FdWorkbenchVO {

    @Schema(description = "待办总数")
    private Long todoTotal;

    @Schema(description = "待办已完成(占位，可扩展)")
    private Long todoDone;

    @Schema(description = "运营模块数")
    private Long moduleCount;

    @Schema(description = "App用户总数")
    private Long totalUsers;

    @Schema(description = "待办列表")
    private List<FdTodoItemVO> todos;

    @Schema(description = "最新动态")
    private List<FdActivityItemVO> activities;
}
