package org.jeecg.modules.member.service.app.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.jeecg.modules.member.entity.FdPaymentRecord;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.IFdPaymentRecordService;
import org.jeecg.modules.member.service.app.IMemberPaymentAppService;
import org.jeecg.modules.member.service.core.MemberSubscriptionGrantService;
import org.jeecg.modules.member.service.core.PointsCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MemberPaymentAppServiceImpl implements IMemberPaymentAppService {

    @Resource
    private IFdMemberOrderService orderService;
    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IFdPaymentRecordService paymentRecordService;
    @Resource
    private MemberSubscriptionGrantService subscriptionGrantService;
    @Resource
    private PointsCoreService pointsCoreService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FdMemberOrder confirmPaid(String orderNo) {
        LambdaQueryWrapper<FdMemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberOrder::getOrderNo, orderNo);
        FdMemberOrder order = orderService.getOne(wrapper, false);
        if (order == null) {
            throw new JeecgBootException("订单不存在");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return order;
        }

        FdMemberPlan plan = planService.getById(order.getPlanId());
        if (plan == null) {
            throw new JeecgBootException("套餐不存在");
        }

        long now = System.currentTimeMillis();
        FdMemberSubscription sub = subscriptionGrantService.grantMemberDays(
                order.getUserId(), plan.getDurationDays(), plan.getPlanCode(),
                order.getPayChannel() != null ? order.getPayChannel() : "PAYMENT");

        if (plan.getBonusPoints() != null && plan.getBonusPoints() > 0) {
            pointsCoreService.addPoints(order.getUserId(), plan.getBonusPoints(),
                    "MEMBER_GIFT", order.getOrderNo(), "开通会员赠送积分");
        }

        order.setPayStatus(1);
        order.setPaidTime(now);
        order.setSubscriptionId(sub.getId());
        order.setUpdateTime(now);
        orderService.updateById(order);

        FdPaymentRecord payment = new FdPaymentRecord();
        payment.setPaymentNo("PM" + now + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        payment.setOrderNo(order.getOrderNo());
        payment.setOrderType("MEMBER");
        payment.setUserId(order.getUserId());
        payment.setPayChannel(order.getPayChannel());
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setPayStatus(1);
        payment.setNotifyTime(now);
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        paymentRecordService.save(payment);

        return order;
    }
}
