package org.jeecg.modules.member.util;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.api.IMemberUserResolver;
import org.springframework.stereotype.Component;

@Component
public class MemberAuthHelper {

    @Resource
    private IMemberUserResolver userResolver;

    public Long resolveUserId(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            throw new JeecgBootException("请先登录");
        }
        return userResolver.resolveUserId(authorization);
    }
}
