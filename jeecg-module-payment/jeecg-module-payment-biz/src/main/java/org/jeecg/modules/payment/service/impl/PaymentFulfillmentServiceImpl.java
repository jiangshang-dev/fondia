package org.jeecg.modules.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.payment.api.IMemberOrderFeignClient;
import org.jeecg.modules.payment.config.FondiaPaymentProperties;
import org.jeecg.modules.payment.constant.PayChannel;
import org.jeecg.modules.payment.dto.PaymentVerifyDTO;
import org.jeecg.modules.payment.service.IPaymentFulfillmentService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentFulfillmentServiceImpl implements IPaymentFulfillmentService {

    @Resource
    private IMemberOrderFeignClient memberOrderFeignClient;
    @Resource
    private FondiaPaymentProperties properties;

    @Override
    public JSONObject loadOrder(String orderNo) {
        Result<JSONObject> result = memberOrderFeignClient.getOrder(orderNo, properties.getInternalSecret());
        if (result == null || !result.isSuccess() || result.getResult() == null) {
            throw new JeecgBootException(result != null ? result.getMessage() : "订单不存在");
        }
        return result.getResult();
    }

    @Override
    public JSONObject fulfill(String orderNo, String externalTradeNo, String payChannel, String externalPayload) {
        Result<JSONObject> result = memberOrderFeignClient.fulfillOrder(
                orderNo,
                externalTradeNo,
                payChannel,
                externalPayload,
                properties.getInternalSecret());
        if (result == null || !result.isSuccess() || result.getResult() == null) {
            throw new JeecgBootException(result != null ? result.getMessage() : "履约失败");
        }
        return result.getResult();
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
            // P2/P3: 接入 Apple / Google 服务端验单；当前先记录客户端凭证
            if (oConvertUtils.isEmpty(dto.getReceipt())) {
                throw new JeecgBootException("缺少支付凭证");
            }
            externalTradeNo = dto.getReceipt();
        }

        return fulfill(dto.getOrderNo(), externalTradeNo, channel, dto.getReceipt());
    }
}
