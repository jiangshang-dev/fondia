package org.jeecg.modules.payment.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "jeecg-member", contextId = "paymentMemberOrderFeign")
public interface IMemberOrderFeignClient {

    String INTERNAL_SECRET_HEADER = "X-Payment-Internal-Secret";

    @GetMapping("/member/internal/order")
    Result<JSONObject> getOrder(@RequestParam("orderNo") String orderNo,
                                @RequestHeader(INTERNAL_SECRET_HEADER) String secret);

    @PostMapping("/member/internal/order/fulfill")
    Result<JSONObject> fulfillOrder(@RequestParam("orderNo") String orderNo,
                                    @RequestParam("externalTradeNo") String externalTradeNo,
                                    @RequestParam("payChannel") String payChannel,
                                    @RequestParam(value = "externalPayload", required = false) String externalPayload,
                                    @RequestHeader(INTERNAL_SECRET_HEADER) String secret);
}
