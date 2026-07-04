package org.jeecg.modules.payment.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.payment.channel.AlipayPaymentChannel;
import org.jeecg.modules.payment.constant.PayChannel;
import org.jeecg.modules.payment.service.IPaymentFulfillmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "支付回调")
@RestController
@RequestMapping("/payment/notify")
public class PaymentNotifyController {

    @Resource
    private AlipayPaymentChannel alipayPaymentChannel;
    @Resource
    private IPaymentFulfillmentService fulfillmentService;

    @Operation(summary = "支付宝异步通知")
    @PostMapping("/alipay")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        log.info("[AlipayNotify] out_trade_no={} trade_status={}",
                params.get("out_trade_no"), params.get("trade_status"));

        if (!alipayPaymentChannel.verifyNotify(params)) {
            log.warn("[AlipayNotify] signature invalid");
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "success";
        }
        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        try {
            fulfillmentService.fulfill(orderNo, tradeNo, PayChannel.ALIPAY, params.toString());
            return "success";
        } catch (Exception e) {
            log.error("[AlipayNotify] fulfill failed orderNo={}", orderNo, e);
            return "failure";
        }
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }
}
