package org.jeecg.modules.member.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "jeecg-user", contextId = "memberUserFeign")
public interface IUserFeignClient {
    @GetMapping("/user/auth/info")
    Result<JSONObject> getUserInfo(@RequestHeader("Authorization") String authorization);
}
