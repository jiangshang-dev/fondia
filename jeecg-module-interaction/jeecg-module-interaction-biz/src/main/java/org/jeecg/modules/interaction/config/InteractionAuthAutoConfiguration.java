package org.jeecg.modules.interaction.config;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.interaction.api.IInteractionUserResolver;
import org.jeecg.modules.interaction.api.IUserBriefResolver;
import org.jeecg.modules.interaction.api.InteractionUserBrief;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 互动模块认证自动配置（单体模式：同进程调用 user 模块）
 */
@AutoConfiguration
@ConditionalOnClass(IFdUserService.class)
public class InteractionAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IInteractionUserResolver.class)
    public IInteractionUserResolver localInteractionUserResolver(IFdUserService fdUserService) {
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

    @Bean
    @ConditionalOnMissingBean(IUserBriefResolver.class)
    public IUserBriefResolver localUserBriefResolver(IFdUserService fdUserService) {
        return userId -> {
            FdUser user = fdUserService.getUserById(userId);
            if (user == null) {
                return InteractionUserBrief.builder()
                        .id(userId)
                        .nickname("用户" + userId)
                        .build();
            }
            return InteractionUserBrief.builder()
                    .id(user.getId())
                    .nickname(resolveNickname(user))
                    .avatar(user.getAvatar())
                    .build();
        };
    }

    private String resolveNickname(FdUser user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return "用户" + user.getId();
    }
}
