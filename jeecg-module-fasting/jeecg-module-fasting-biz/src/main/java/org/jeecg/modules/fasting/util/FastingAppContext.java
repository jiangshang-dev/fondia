package org.jeecg.modules.fasting.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.service.IFdUserService;

public final class FastingAppContext {

    public static final String DEVICE_ID_HEADER = "X-Device-Id";

    private FastingAppContext() {
    }

    public static String resolveDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader(DEVICE_ID_HEADER);
        if (oConvertUtils.isEmpty(deviceId)) {
            deviceId = request.getParameter("deviceId");
        }
        return deviceId;
    }

    public static Long resolveUserId(HttpServletRequest request, IFdUserService userService) {
        String token = request.getHeader("Authorization");
        if (oConvertUtils.isEmpty(token)) {
            return null;
        }
        try {
            String username = JwtUtil.getUsername(token);
            if (oConvertUtils.isEmpty(username)) {
                return null;
            }
            FdUser user = userService.getOne(new LambdaQueryWrapper<FdUser>()
                    .eq(FdUser::getUsername, username)
                    .or()
                    .eq(FdUser::getEmail, username)
                    .or()
                    .eq(FdUser::getPhone, username)
                    .last("LIMIT 1"));
            return user != null ? user.getId() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
