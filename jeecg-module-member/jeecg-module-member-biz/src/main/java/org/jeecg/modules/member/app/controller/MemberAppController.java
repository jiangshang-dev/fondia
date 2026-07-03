package org.jeecg.modules.member.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.IFdMemberSubscriptionService;
import org.jeecg.modules.member.service.app.IMemberOrderAppService;
import org.jeecg.modules.member.service.app.IMemberPaymentAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App会员")
@RestController
@RequestMapping("/member")
public class MemberAppController {

    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IFdMemberSubscriptionService subscriptionService;
    @Resource
    private IMemberOrderAppService orderAppService;
    @Resource
    private IMemberPaymentAppService paymentAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "可购会员套餐列表")
    @GetMapping("/plans")
    public Result<List<FdMemberPlan>> plans(@RequestParam(required = false) String platform) {
        LambdaQueryWrapper<FdMemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberPlan::getStatus, 1)
                .orderByAsc(FdMemberPlan::getSortNo);
        if (platform != null && !platform.isBlank()) {
            wrapper.and(w -> w.eq(FdMemberPlan::getPlatformScope, "ALL")
                    .or().eq(FdMemberPlan::getPlatformScope, platform.toUpperCase()));
        }
        return Result.OK(planService.list(wrapper));
    }

    @Operation(summary = "当前用户会员状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        long now = System.currentTimeMillis();
        LambdaQueryWrapper<FdMemberSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberSubscription::getUserId, userId)
                .eq(FdMemberSubscription::getStatus, 1)
                .gt(FdMemberSubscription::getExpireTime, now)
                .orderByDesc(FdMemberSubscription::getExpireTime)
                .last("LIMIT 1");
        FdMemberSubscription sub = subscriptionService.getOne(wrapper, false);
        Map<String, Object> data = new HashMap<>();
        data.put("isMember", sub != null);
        data.put("subscription", sub);
        return Result.OK(data);
    }

    @Operation(summary = "创建会员订单")
    @PostMapping("/order/create")
    public Result<FdMemberOrder> createOrder(@RequestHeader("Authorization") String authorization,
                                             @RequestParam Long planId,
                                             @RequestParam(defaultValue = "ALIPAY") String payChannel,
                                             @RequestParam(required = false) String clientPlatform) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(orderAppService.createOrder(userId, planId, payChannel, clientPlatform));
    }

    @Operation(summary = "支付成功确认（开发/回调用）")
    @PostMapping("/order/pay/confirm")
    public Result<FdMemberOrder> confirmPay(@RequestHeader("Authorization") String authorization,
                                            @RequestParam String orderNo) {
        authHelper.resolveUserId(authorization);
        return Result.OK(paymentAppService.confirmPaid(orderNo));
    }
}
