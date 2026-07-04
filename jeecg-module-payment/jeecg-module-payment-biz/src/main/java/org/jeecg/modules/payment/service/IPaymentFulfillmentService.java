package org.jeecg.modules.payment.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.payment.dto.PaymentVerifyDTO;
import org.jeecg.modules.payment.vo.PaymentPrepayVO;

public interface IPaymentFulfillmentService {

    JSONObject loadOrder(String orderNo);

    JSONObject fulfill(String orderNo, String externalTradeNo, String payChannel, String externalPayload);

    JSONObject verifyAndFulfill(Long userId, PaymentVerifyDTO dto);
}
