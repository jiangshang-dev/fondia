package org.jeecg.modules.payment.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.payment.vo.PaymentPrepayVO;

public interface IPaymentPrepayService {

    PaymentPrepayVO prepay(Long userId, String orderNo, String payChannel);
}
