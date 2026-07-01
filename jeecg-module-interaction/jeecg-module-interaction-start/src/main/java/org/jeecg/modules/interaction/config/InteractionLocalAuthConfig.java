package org.jeecg.modules.interaction.config;

import org.jeecg.modules.interaction.api.IInteractionUserResolver;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 单体模式：同进程直接调用 user 模块服务解析用户
 */
@Configuration
@ConditionalOnClass(IFdUserService.class)
public class InteractionLocalAuthConfig {

    @Bean
    @Primary
    public IInteractionUserResolver localInteractionUserResolver(IFdUserService fdUserService) {
        return authorization -> {
            UserInfoVO userInfo = fdUserService.getUserInfo(authorization);
            if (userInfo == null || userInfo.getId() == null) {
                throw new org.jeecg.common.exception.JeecgBootException("用户不存在");
            }
            return userInfo.getId();
        };
    }
}
