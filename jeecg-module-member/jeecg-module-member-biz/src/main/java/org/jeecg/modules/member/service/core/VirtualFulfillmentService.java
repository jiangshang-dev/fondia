package org.jeecg.modules.member.service.core;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VirtualFulfillmentService {

    @Resource
    private MemberSubscriptionGrantService subscriptionGrantService;

    public Map<String, Object> fulfill(Long userId, String virtualConfigJson) {
        if (oConvertUtils.isEmpty(virtualConfigJson)) {
            throw new JeecgBootException("虚拟商品未配置履约规则");
        }
        JSONObject config = JSONObject.parseObject(virtualConfigJson);
        String fulfillType = config.getString("fulfillType");
        if (oConvertUtils.isEmpty(fulfillType)) {
            throw new JeecgBootException("虚拟商品履约类型未配置");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fulfillType", fulfillType);

        switch (fulfillType) {
            case "MEMBER_DAYS" -> {
                int memberDays = config.getIntValue("memberDays");
                String planCode = config.getString("planCode");
                FdMemberSubscription sub = subscriptionGrantService.grantMemberDays(
                        userId, memberDays, planCode, "MALL_REDEEM");
                result.put("memberDays", memberDays);
                result.put("subscription", sub);
                result.put("message", "已开通" + memberDays + "天会员");
            }
            case "COUPON" -> {
                String couponCode = "CP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                result.put("couponCode", couponCode);
                result.put("couponAmount", config.get("couponAmount"));
                result.put("message", "优惠券已发放：" + couponCode);
            }
            default -> throw new JeecgBootException("不支持的虚拟履约类型：" + fulfillType);
        }
        return result;
    }
}
