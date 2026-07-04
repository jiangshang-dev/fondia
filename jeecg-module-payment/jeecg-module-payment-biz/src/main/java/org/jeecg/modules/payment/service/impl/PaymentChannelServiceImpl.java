package org.jeecg.modules.payment.service.impl;

import org.jeecg.modules.payment.constant.PayChannel;
import org.jeecg.modules.payment.constant.PayRegion;
import org.jeecg.modules.payment.service.IPaymentChannelService;
import org.jeecg.modules.payment.vo.PaymentChannelVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PaymentChannelServiceImpl implements IPaymentChannelService {

    @Override
    public Map<String, Object> listChannels(String platform, String region) {
        List<PaymentChannelVO> channels = resolveChannels(platform, region);
        Map<String, Object> data = new HashMap<>(4);
        data.put("region", region);
        data.put("platform", platform);
        data.put("channels", channels);
        return data;
    }

    @Override
    public List<PaymentChannelVO> resolveChannels(String platform, String region) {
        String pf = platform == null ? "" : platform.toLowerCase(Locale.ROOT);
        boolean cn = PayRegion.CN.equalsIgnoreCase(region);
        List<PaymentChannelVO> channels = new ArrayList<>();

        switch (pf) {
            case "ios" -> {
                channels.add(channel(PayChannel.APPLE_IAP, "App Store", "Apple 内购订阅", true));
                if (cn) {
                    channels.add(channel(PayChannel.ALIPAY, "支付宝", "跳转支付宝客户端", false));
                }
            }
            case "android" -> {
                if (cn) {
                    channels.add(channel(PayChannel.ALIPAY, "支付宝", "跳转支付宝客户端", true));
                } else {
                    channels.add(channel(PayChannel.GOOGLE_PLAY, "Google Play", "Google Play 结算", true));
                }
            }
            default -> channels.add(channel(PayChannel.ALIPAY, "支付宝", "网页/扫码支付", true));
        }
        return channels;
    }

    private PaymentChannelVO channel(String code, String name, String subtitle, boolean selected) {
        return PaymentChannelVO.builder()
                .code(code)
                .name(name)
                .subtitle(subtitle)
                .defaultSelected(selected)
                .build();
    }
}
