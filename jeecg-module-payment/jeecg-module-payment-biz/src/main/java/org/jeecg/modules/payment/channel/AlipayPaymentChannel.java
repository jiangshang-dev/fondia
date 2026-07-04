package org.jeecg.modules.payment.channel;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.payment.config.FondiaPaymentProperties;
import org.jeecg.modules.payment.constant.PayChannel;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Slf4j
@Component
public class AlipayPaymentChannel {

    @Resource
    private FondiaPaymentProperties properties;

    public String createAppPayOrderString(JSONObject order) {
        if (properties.isMockEnabled()) {
            return "MOCK_ALIPAY_" + order.getString("orderNo");
        }
        FondiaPaymentProperties.Alipay cfg = properties.getAlipay();
        if (oConvertUtils.isEmpty(cfg.getAppId())
                || oConvertUtils.isEmpty(cfg.getPrivateKey())
                || oConvertUtils.isEmpty(cfg.getAlipayPublicKey())) {
            throw new JeecgBootException("支付宝未配置，请开启 fondia.payment.mock-enabled 或补齐密钥");
        }

        AlipayClient client = new DefaultAlipayClient(
                cfg.getGatewayUrl(),
                cfg.getAppId(),
                cfg.getPrivateKey(),
                "json",
                cfg.getCharset(),
                cfg.getAlipayPublicKey(),
                cfg.getSignType());

        JSONObject biz = new JSONObject();
        biz.put("out_trade_no", order.getString("orderNo"));
        biz.put("total_amount", order.getBigDecimal("amount").setScale(2, RoundingMode.HALF_UP).toPlainString());
        biz.put("subject", order.getString("planName"));
        biz.put("product_code", "QUICK_MSECURITY_PAY");

        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizContent(biz.toJSONString());
        if (oConvertUtils.isNotEmpty(cfg.getNotifyUrl())) {
            request.setNotifyUrl(cfg.getNotifyUrl());
        }
        try {
            AlipayTradeAppPayResponse response = client.sdkExecute(request);
            if (!response.isSuccess()) {
                throw new JeecgBootException("支付宝预下单失败：" + response.getSubMsg());
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("[Alipay] prepay failed orderNo={}", order.getString("orderNo"), e);
            throw new JeecgBootException("支付宝预下单异常");
        }
    }

    public boolean verifyNotify(java.util.Map<String, String> params) {
        if (properties.isMockEnabled()) {
            return true;
        }
        try {
            FondiaPaymentProperties.Alipay cfg = properties.getAlipay();
            return com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params,
                    cfg.getAlipayPublicKey(),
                    cfg.getCharset(),
                    cfg.getSignType());
        } catch (Exception e) {
            log.error("[Alipay] notify verify failed", e);
            return false;
        }
    }

    public String channelCode() {
        return PayChannel.ALIPAY;
    }
}
