package org.jeecg.config;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.interaction.api.IInteractionUserResolver;
import org.jeecg.modules.interaction.api.IUserBriefResolver;
import org.jeecg.modules.interaction.api.IUserFeignClient;
import org.jeecg.modules.interaction.api.InteractionUserBrief;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微服务启动器：注册 Feign 用户解析器
 */
@Configuration
public class InteractionCloudAuthConfiguration {

    @Bean
    @ConditionalOnMissingBean(IInteractionUserResolver.class)
    public IInteractionUserResolver feignInteractionUserResolver(IUserFeignClient userFeignClient) {
        return authorization -> {
            if (oConvertUtils.isEmpty(authorization)) {
                throw new JeecgBootException("请先登录");
            }
            Result<JSONObject> result = userFeignClient.getUserInfo(authorization);
            if (result == null || !result.isSuccess() || result.getResult() == null) {
                throw new JeecgBootException(result != null ? result.getMessage() : "获取用户信息失败");
            }
            Long userId = result.getResult().getLong("id");
            if (userId == null) {
                throw new JeecgBootException("用户不存在");
            }
            return userId;
        };
    }

    @Bean
    @ConditionalOnMissingBean(IUserBriefResolver.class)
    public IUserBriefResolver feignUserBriefResolver(IUserFeignClient userFeignClient) {
        return userId -> {
            Result<JSONObject> result = userFeignClient.getUserBrief(userId);
            if (result == null || !result.isSuccess() || result.getResult() == null) {
                return InteractionUserBrief.builder()
                        .id(userId)
                        .nickname("用户" + userId)
                        .build();
            }
            JSONObject data = result.getResult();
            return InteractionUserBrief.builder()
                    .id(data.getLong("id"))
                    .nickname(data.getString("nickname"))
                    .avatar(data.getString("avatar"))
                    .build();
        };
    }
}
