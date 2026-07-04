package org.jeecg.modules.payment.service;

import org.jeecg.modules.payment.vo.PaymentChannelVO;

import java.util.List;
import java.util.Map;

public interface IPaymentChannelService {

    Map<String, Object> listChannels(String platform, String region);

    List<PaymentChannelVO> resolveChannels(String platform, String region);
}
