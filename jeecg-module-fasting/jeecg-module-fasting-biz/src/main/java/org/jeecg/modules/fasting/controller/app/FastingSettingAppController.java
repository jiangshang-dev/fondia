package org.jeecg.modules.fasting.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fasting.dto.FastSettingSaveDTO;
import org.jeecg.modules.fasting.service.IFdFastUserSettingService;
import org.jeecg.modules.fasting.util.FastingAppContext;
import org.jeecg.modules.fasting.vo.FastSettingVO;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Fast16-设置")
@RestController
@RequestMapping("/fasting/app/setting")
public class FastingSettingAppController {

    @Resource
    private IFdFastUserSettingService settingService;
    @Resource
    private IFdUserService userService;

    @Operation(summary = "获取设置")
    @GetMapping
    public Result<FastSettingVO> getSetting(HttpServletRequest request) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        return Result.OK(settingService.getSetting(userId, deviceId));
    }

    @Operation(summary = "保存设置")
    @PutMapping
    public Result<FastSettingVO> saveSetting(HttpServletRequest request,
                                             @Validated @RequestBody FastSettingSaveDTO dto) {
        Long userId = FastingAppContext.resolveUserId(request, userService);
        String deviceId = FastingAppContext.resolveDeviceId(request);
        return Result.OK(settingService.saveSetting(userId, deviceId, dto));
    }
}
