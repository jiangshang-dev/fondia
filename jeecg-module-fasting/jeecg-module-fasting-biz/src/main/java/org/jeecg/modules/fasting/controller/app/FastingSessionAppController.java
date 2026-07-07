package org.jeecg.modules.fasting.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fasting.dto.FastSessionEndDTO;
import org.jeecg.modules.fasting.dto.FastSessionStartDTO;
import org.jeecg.modules.fasting.dto.FastSessionSyncDTO;
import org.jeecg.modules.fasting.service.IFdFastSessionService;
import org.jeecg.modules.fasting.util.FastingAppContext;
import org.jeecg.modules.fasting.vo.FastSessionVO;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Fast16-会话")
@RestController
@RequestMapping("/fasting/app/session")
public class FastingSessionAppController {

    @Resource
    private IFdFastSessionService sessionService;
    @Resource
    private IFdUserService userService;

    @Operation(summary = "开始会话")
    @PostMapping("/start")
    public Result<FastSessionVO> start(HttpServletRequest request,
                                       @Validated @RequestBody FastSessionStartDTO dto) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        return Result.OK(sessionService.startSession(userId, deviceId, dto));
    }

    @Operation(summary = "结束会话")
    @PostMapping("/end")
    public Result<FastSessionVO> end(HttpServletRequest request,
                                     @RequestBody FastSessionEndDTO dto) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        return Result.OK(sessionService.endSession(userId, deviceId, dto));
    }

    @Operation(summary = "当前进行中会话")
    @GetMapping("/current")
    public Result<FastSessionVO> current(HttpServletRequest request) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        return Result.OK(sessionService.getCurrentSession(userId, deviceId));
    }

    @Operation(summary = "批量同步")
    @PostMapping("/sync")
    public Result<String> sync(HttpServletRequest request, @RequestBody FastSessionSyncDTO dto) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        sessionService.syncSessions(userId, deviceId, dto);
        return Result.OK("同步成功");
    }
}
