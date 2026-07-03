package org.jeecg.modules.member.config;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.api.IMemberUserResolver;
import org.jeecg.modules.member.api.IUserFeignClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(FeignAutoConfiguration.class)
@ConditionalOnClass(IUserFeignClient.class)
public class MemberAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IMemberUserResolver.class)
    public IMemberUserResolver feignMemberUserResolver(IUserFeignClient userFeignClient) {
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
}
