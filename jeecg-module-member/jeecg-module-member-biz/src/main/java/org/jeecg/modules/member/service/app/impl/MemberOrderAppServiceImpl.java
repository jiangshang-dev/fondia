package org.jeecg.modules.member.service.app.impl;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.app.IMemberOrderAppService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MemberOrderAppServiceImpl implements IMemberOrderAppService {

    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IFdMemberOrderService orderService;

    @Override
    public FdMemberOrder createOrder(Long userId, Long planId, String payChannel, String clientPlatform) {
        FdMemberPlan plan = planService.getById(planId);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            throw new JeecgBootException("套餐不存在或已下架");
        }
        long now = System.currentTimeMillis();
        FdMemberOrder order = new FdMemberOrder();
        order.setOrderNo("MO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setUserId(userId);
        order.setPlanId(plan.getId());
        order.setPlanCode(plan.getPlanCode());
        order.setPlanName(plan.getName());
        order.setDurationDays(plan.getDurationDays());
        order.setAmount(plan.getPrice());
        order.setCurrency(plan.getCurrency());
        order.setPayChannel(payChannel);
        order.setPayStatus(0);
        order.setClientPlatform(clientPlatform);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        orderService.save(order);
        return order;
    }
}
