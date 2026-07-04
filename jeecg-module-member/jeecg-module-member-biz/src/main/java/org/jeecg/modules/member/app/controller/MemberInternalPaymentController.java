package org.jeecg.modules.member.app.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.config.FondiaMemberPaymentProperties;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.app.IMemberPaymentAppService;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Slf4j
@Tag(name = "App会员-支付内部")
@RestController
@RequestMapping("/member/internal")
public class MemberInternalPaymentController {

    public static final String INTERNAL_SECRET_HEADER = "X-Payment-Internal-Secret";

    @Resource
    private FondiaMemberPaymentProperties paymentProperties;
    @Resource
    private IFdMemberOrderService orderService;
    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IMemberPaymentAppService paymentAppService;

    @GetMapping("/order")
    public Result<JSONObject> getOrder(@RequestParam String orderNo,
                                       @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        checkSecret(secret);
        FdMemberOrder order = findOrder(orderNo);
        FdMemberPlan plan = planService.getById(order.getPlanId());
        JSONObject data = (JSONObject) JSONObject.toJSON(order);
        if (plan != null) {
            data.put("appleProductId", plan.getAppleProductId());
            data.put("googleProductId", plan.getGoogleProductId());
        }
        return Result.OK(data);
    }

    @PostMapping("/order/fulfill")
    public Result<JSONObject> fulfill(@RequestParam String orderNo,
                                      @RequestParam String externalTradeNo,
                                      @RequestParam String payChannel,
                                      @RequestParam(required = false) String externalPayload,
                                      @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        checkSecret(secret);
        FdMemberOrder order = paymentAppService.confirmPaidFromPayment(
                orderNo, externalTradeNo, payChannel, externalPayload);
        return Result.OK((JSONObject) JSONObject.toJSON(order));
    }

    private void checkSecret(String secret) {
        if (oConvertUtils.isEmpty(secret)
                || !secret.equals(paymentProperties.getInternalSecret())) {
            throw new JeecgBootException("非法的内部调用");
        }
    }

    private FdMemberOrder findOrder(String orderNo) {
        FdMemberOrder order = orderService.getOne(new LambdaQueryWrapper<FdMemberOrder>()
                .eq(FdMemberOrder::getOrderNo, orderNo), false);
        if (order == null) {
            throw new JeecgBootException("订单不存在");
        }
        return order;
    }
}
