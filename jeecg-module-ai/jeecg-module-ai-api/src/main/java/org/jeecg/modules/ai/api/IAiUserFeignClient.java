package org.jeecg.modules.ai.api;

import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 用户服务 Feign：解析 App Authorization。
 */
@FeignClient(name = "jeecg-user", contextId = "aiUserFeignClient")
public interface IAiUserFeignClient {

    @GetMapping("/user/auth/info")
    Result<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorization);
}
