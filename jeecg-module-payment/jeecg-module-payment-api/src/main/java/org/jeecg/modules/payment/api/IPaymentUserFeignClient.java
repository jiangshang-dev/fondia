package org.jeecg.modules.payment.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "jeecg-user", contextId = "paymentUserFeign")
public interface IPaymentUserFeignClient {

    @GetMapping("/user/auth/info")
    Result<JSONObject> getUserInfo(@RequestHeader("Authorization") String authorization);
}
