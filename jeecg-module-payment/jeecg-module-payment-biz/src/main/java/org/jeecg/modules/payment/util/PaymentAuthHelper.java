package org.jeecg.modules.payment.util;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.payment.api.IPaymentUserFeignClient;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuthHelper {

    @Resource
    private IPaymentUserFeignClient userFeignClient;

    public Long resolveUserId(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            throw new JeecgBootException("请先登录");
        }
        Result<JSONObject> result = userFeignClient.getUserInfo(authorization);
        if (result == null || !result.isSuccess() || result.getResult() == null) {
            throw new JeecgBootException(result != null ? result.getMessage() : "获取用户信息失败");
        }
        Long userId = result.getResult().getLong("id");
        if (userId == null) {
            throw new JeecgBootException("用户不存在");
        }
        return userId;
    }
}
