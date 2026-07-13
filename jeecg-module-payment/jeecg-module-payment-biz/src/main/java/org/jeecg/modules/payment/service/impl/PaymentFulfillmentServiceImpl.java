package org.jeecg.modules.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.app.IMemberPaymentAppService;
import org.jeecg.modules.payment.config.FondiaPaymentProperties;
import org.jeecg.modules.payment.constant.PayChannel;
import org.jeecg.modules.payment.dto.PaymentVerifyDTO;
import org.jeecg.modules.payment.service.IPaymentFulfillmentService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentFulfillmentServiceImpl implements IPaymentFulfillmentService {

    @Resource
    private IFdMemberOrderService orderService;
    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IMemberPaymentAppService paymentAppService;
    @Resource
    private FondiaPaymentProperties properties;

    @Override
    public JSONObject loadOrder(String orderNo) {
        FdMemberOrder order = findOrder(orderNo);
        FdMemberPlan plan = planService.getById(order.getPlanId());
        JSONObject data = (JSONObject) JSONObject.toJSON(order);
        if (plan != null) {
            data.put("appleProductId", plan.getAppleProductId());
            data.put("googleProductId", plan.getGoogleProductId());
        }
        return data;
    }

    @Override
    public JSONObject fulfill(String orderNo, String externalTradeNo, String payChannel, String externalPayload) {
        FdMemberOrder order = paymentAppService.confirmPaidFromPayment(
                orderNo, externalTradeNo, payChannel, externalPayload);
        return (JSONObject) JSONObject.toJSON(order);
    }

    @Override
    public JSONObject verifyAndFulfill(Long userId, PaymentVerifyDTO dto) {
        JSONObject order = loadOrder(dto.getOrderNo());
        Long orderUserId = order.getLong("userId");
        if (orderUserId == null || !orderUserId.equals(userId)) {
            throw new JeecgBootException("无权操作该订单");
        }
        Integer payStatus = order.getInteger("payStatus");
        if (payStatus != null && payStatus == 1) {
            return order;
        }
        String channel = dto.getPayChannel();
        if (oConvertUtils.isEmpty(channel)) {
            channel = order.getString("payChannel");
        }
        String externalTradeNo = dto.getReceipt();
        if (oConvertUtils.isEmpty(externalTradeNo)) {
            externalTradeNo = "CLIENT_" + dto.getOrderNo();
        }

        if (PayChannel.ALIPAY.equals(channel) && properties.isMockEnabled()) {
            externalTradeNo = "MOCK_ALIPAY_" + dto.getOrderNo();
        } else if (PayChannel.APPLE_IAP.equals(channel) || PayChannel.GOOGLE_PLAY.equals(channel)) {
            if (oConvertUtils.isEmpty(dto.getReceipt())) {
                throw new JeecgBootException("缺少支付凭证");
            }
            externalTradeNo = dto.getReceipt();
        }

        return fulfill(dto.getOrderNo(), externalTradeNo, channel, dto.getReceipt());
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
