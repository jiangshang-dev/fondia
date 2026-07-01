package org.jeecg.modules.interaction.config;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.interaction.api.IInteractionUserResolver;
import org.jeecg.modules.interaction.api.IUserBriefResolver;
import org.jeecg.modules.interaction.api.IUserFeignClient;
import org.jeecg.modules.interaction.api.InteractionUserBrief;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 互动模块认证自动配置（微服务 Feign 解析用户）
 */
@AutoConfiguration
@AutoConfigureAfter(FeignAutoConfiguration.class)
@ConditionalOnClass(IUserFeignClient.class)
public class InteractionAuthAutoConfiguration {

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
