package org.jeecg.modules.fasting.util;

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

    /**
     * 从 Authorization / X-Access-Token 解析 App 用户（fd_user）ID，并校验 JWT。
     */
    public static Long resolveUserId(HttpServletRequest request, IFdUserService userService) {
        String token = extractToken(request.getHeader("Authorization"));
        if (oConvertUtils.isEmpty(token)) {
            token = extractToken(request.getHeader("X-Access-Token"));
        }
        if (oConvertUtils.isEmpty(token)) {
            return null;
        }
        try {
            String username = JwtUtil.getUsername(token);
            if (oConvertUtils.isEmpty(username)) {
                return null;
            }
            FdUser user = userService.getUserByAccount(username);
            if (user == null) {
                try {
                    user = userService.getUserById(Long.parseLong(username));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            if (user == null || !JwtUtil.verify(token, username, user.getPassword())) {
                return null;
            }
            return user.getId();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String extractToken(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            return null;
        }
        String value = authorization.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return value.substring(7).trim();
        }
        return value;
    }
}
