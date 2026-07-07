package org.jeecg.modules.fasting.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fasting.service.IFdFastSessionService;
import org.jeecg.modules.fasting.util.FastingAppContext;
import org.jeecg.modules.fasting.vo.FastDailyStatVO;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Fast16-统计")
@RestController
@RequestMapping("/fasting/app/stats")
public class FastingStatsAppController {

    @Resource
    private IFdFastSessionService sessionService;
    @Resource
    private IFdUserService userService;

    @Operation(summary = "近7天统计")
    @GetMapping("/weekly")
    public Result<List<FastDailyStatVO>> weekly(HttpServletRequest request) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        return Result.OK(sessionService.getWeeklyStats(userId, deviceId));
    }
}
