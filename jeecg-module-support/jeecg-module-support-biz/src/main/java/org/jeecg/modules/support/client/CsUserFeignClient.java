package org.jeecg.modules.support.client;

import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "jeecg-user", contextId = "csUserFeignClient")
public interface CsUserFeignClient {

    @GetMapping("/user/auth/info")
    Result<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorization);
}
