package org.jeecg.modules.member.service.app;

import org.jeecg.modules.member.entity.FdMemberOrder;

public interface IMemberPaymentAppService {
    FdMemberOrder confirmPaid(String orderNo);
}
