package org.jeecg.modules.payment.util;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuthHelper {

    @Resource
    private IFdUserService fdUserService;

    public Long resolveUserId(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            throw new JeecgBootException("请先登录");
        }
        UserInfoVO userInfo = fdUserService.getUserInfo(authorization);
        if (userInfo == null || userInfo.getId() == null) {
            throw new JeecgBootException("用户不存在");
        }
        return userInfo.getId();
    }
}
