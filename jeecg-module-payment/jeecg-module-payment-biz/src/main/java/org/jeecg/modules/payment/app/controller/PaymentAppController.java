package org.jeecg.modules.payment.app.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.payment.dto.PaymentPrepayDTO;
import org.jeecg.modules.payment.dto.PaymentVerifyDTO;
import org.jeecg.modules.payment.service.IPaymentChannelService;
import org.jeecg.modules.payment.service.IPaymentFulfillmentService;
import org.jeecg.modules.payment.service.IPaymentPrepayService;
import org.jeecg.modules.payment.util.ClientIpUtils;
import org.jeecg.modules.payment.util.PaymentAuthHelper;
import org.jeecg.modules.payment.util.PaymentRegionResolver;
import org.jeecg.modules.payment.vo.PaymentPrepayVO;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Tag(name = "App支付")
@RestController
@RequestMapping("/payment")
public class PaymentAppController {

    @Resource
    private PaymentAuthHelper authHelper;
    @Resource
    private IPaymentChannelService channelService;
    @Resource
    private IPaymentPrepayService prepayService;
    @Resource
    private IPaymentFulfillmentService fulfillmentService;

    @Operation(summary = "可用支付渠道")
    @GetMapping("/channels")
    public Result<Map<String, Object>> channels(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "android") String platform,
                                                  @RequestHeader(value = "X-Fondia-Region", required = false) String regionHint) {
        String ip = ClientIpUtils.resolve(request);
        String region = oConvertUtils.isNotEmpty(regionHint) ? regionHint.toUpperCase(Locale.ROOT) : PaymentRegionResolver.resolveRegion(ip);
        return Result.OK(channelService.listChannels(normalizePlatform(platform), region));
    }

    @Operation(summary = "预支付（获取调起参数）")
    @PostMapping("/prepay")
    public Result<PaymentPrepayVO> prepay(@RequestHeader("Authorization") String authorization,
                                          @RequestBody PaymentPrepayDTO dto) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(prepayService.prepay(userId, dto.getOrderNo(), dto.getPayChannel()));
    }

    @Operation(summary = "客户端验单/模拟支付确认")
    @PostMapping("/verify")
    public Result<JSONObject> verify(@RequestHeader("Authorization") String authorization,
                                     @RequestBody PaymentVerifyDTO dto) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(fulfillmentService.verifyAndFulfill(userId, dto));
    }

    @Operation(summary = "查询订单支付状态")
    @GetMapping("/order/status")
    public Result<JSONObject> orderStatus(@RequestHeader("Authorization") String authorization,
                                          @RequestParam String orderNo) {
        Long userId = authHelper.resolveUserId(authorization);
        JSONObject order = fulfillmentService.loadOrder(orderNo);
        if (!userId.equals(order.getLong("userId"))) {
            return Result.error("无权查看该订单");
        }
        JSONObject data = new JSONObject();
        data.put("orderNo", order.getString("orderNo"));
        data.put("payStatus", order.getInteger("payStatus"));
        data.put("payChannel", order.getString("payChannel"));
        data.put("amount", order.get("amount"));
        return Result.OK(data);
    }

    private String normalizePlatform(String platform) {
        if (platform == null) {
            return "android";
        }
        return platform.toLowerCase(Locale.ROOT);
    }
}
