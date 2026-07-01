package org.jeecg.modules.interaction.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务 Feign 接口（微服务模式下解析当前登录用户）
 */
@FeignClient(value = "jeecg-user", contextId = "interactionUserFeign")
public interface IUserFeignClient {

    @GetMapping("/user/auth/info")
    Result<JSONObject> getUserInfo(@RequestHeader("Authorization") String authorization);

    @GetMapping("/user/public/brief/{userId}")
    Result<JSONObject> getUserBrief(@PathVariable("userId") Long userId);
}
