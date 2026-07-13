package org.jeecg.modules.member.config;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.api.IMemberUserResolver;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 会员模块认证自动配置（单体模式：同进程调用 user 模块）
 */
@AutoConfiguration
@ConditionalOnClass(IFdUserService.class)
public class MemberAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IMemberUserResolver.class)
    public IMemberUserResolver localMemberUserResolver(IFdUserService fdUserService) {
        return authorization -> {
            if (oConvertUtils.isEmpty(authorization)) {
                throw new JeecgBootException("请先登录");
            }
            UserInfoVO userInfo = fdUserService.getUserInfo(authorization);
            if (userInfo == null || userInfo.getId() == null) {
                throw new JeecgBootException("用户不存在");
            }
            return userInfo.getId();
        };
    }
}
