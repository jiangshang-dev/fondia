package org.jeecg.modules.interaction.config;

import org.jeecg.modules.interaction.api.IUserBriefResolver;
import org.jeecg.modules.interaction.api.InteractionUserBrief;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.service.IFdUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass(IFdUserService.class)
public class InteractionLocalUserBriefConfig {

    @Bean
    @Primary
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
