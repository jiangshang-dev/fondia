package org.jeecg.modules.support.admin.util;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBoot401Exception;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

/**
 * 管理端登录用户解析（Servlet 请求线程专用，勿在异步线程调用）。
 */
@Component
public class CsAdminLoginHelper {

    public LoginUser requireLoginUser() {
        LoginUser user = resolveLoginUser();
        if (user == null || oConvertUtils.isEmpty(user.getId())) {
            throw new JeecgBoot401Exception("未登录或Token无效");
        }
        return user;
    }

    public LoginUser resolveLoginUser() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser loginUser) {
                return loginUser;
            }
        } catch (Exception e) {
            throw new JeecgBoot401Exception("认证服务不可用，请确认 jeecg-support 已加载 Shiro 配置");
        }
        return null;
    }
}
