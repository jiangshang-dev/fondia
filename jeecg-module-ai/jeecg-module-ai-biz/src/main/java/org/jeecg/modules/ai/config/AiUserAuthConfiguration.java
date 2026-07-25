package org.jeecg.modules.ai.config;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.ai.api.IAiUserFeignClient;
import org.jeecg.modules.ai.api.IAiUserResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AiUserAuthConfiguration {

    @Bean
    @ConditionalOnMissingBean(IAiUserResolver.class)
    public IAiUserResolver aiUserResolver(IAiUserFeignClient userFeignClient) {
        return authorization -> {
            if (oConvertUtils.isEmpty(authorization)) {
                throw new JeecgBootException("请先登录");
            }
            Result<Map<String, Object>> result = userFeignClient.getUserInfo(authorization);
            if (result == null || !result.isSuccess() || result.getResult() == null) {
                throw new JeecgBootException(result != null ? result.getMessage() : "获取用户信息失败");
            }
            Object id = result.getResult().get("id");
            if (id == null) {
                throw new JeecgBootException("用户不存在");
            }
            return Long.valueOf(String.valueOf(id));
        };
    }
}
