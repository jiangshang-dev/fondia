package org.jeecg.modules.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.payment.channel.AlipayPaymentChannel;
import org.jeecg.modules.payment.constant.PayChannel;
import org.jeecg.modules.payment.service.IPaymentFulfillmentService;
import org.jeecg.modules.payment.service.IPaymentPrepayService;
import org.jeecg.modules.payment.vo.PaymentPrepayVO;
import org.springframework.stereotype.Service;

@Service
public class PaymentPrepayServiceImpl implements IPaymentPrepayService {

    @Resource
    private IPaymentFulfillmentService fulfillmentService;
    @Resource
    private AlipayPaymentChannel alipayPaymentChannel;

    @Override
    public PaymentPrepayVO prepay(Long userId, String orderNo, String payChannel) {
        JSONObject order = fulfillmentService.loadOrder(orderNo);
        Long orderUserId = order.getLong("userId");
        if (orderUserId == null || !orderUserId.equals(userId)) {
            throw new JeecgBootException("无权操作该订单");
        }
        Integer payStatus = order.getInteger("payStatus");
        if (payStatus != null && payStatus == 1) {
            throw new JeecgBootException("订单已支付");
        }

        String channel = oConvertUtils.isEmpty(payChannel) ? order.getString("payChannel") : payChannel;
        if (oConvertUtils.isEmpty(channel)) {
            channel = PayChannel.ALIPAY;
        }

        PaymentPrepayVO.PaymentPrepayVOBuilder builder = PaymentPrepayVO.builder()
                .orderNo(orderNo)
                .payChannel(channel);

        switch (channel) {
            case PayChannel.ALIPAY -> {
                String orderString = alipayPaymentChannel.createAppPayOrderString(order);
                builder.orderString(orderString).mockPay(orderString.startsWith("MOCK_"));
            }
            case PayChannel.APPLE_IAP -> builder.productId(order.getString("appleProductId")).mockPay(false);
            case PayChannel.GOOGLE_PLAY -> builder.productId(order.getString("googleProductId")).mockPay(false);
            default -> throw new JeecgBootException("暂不支持的支付渠道：" + channel);
        }
        return builder.build();
    }
}
