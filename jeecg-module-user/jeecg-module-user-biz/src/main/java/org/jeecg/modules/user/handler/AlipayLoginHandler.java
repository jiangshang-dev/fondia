package org.jeecg.modules.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.enums.IdentityTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 支付宝登录处理器
 */
@Slf4j
@Component
public class AlipayLoginHandler implements ThirdPartyLoginHandler {

    @Override
    public String getIdentityType() {
        return IdentityTypeEnum.ALIPAY.getCode();
    }

    @Override
    public boolean verifyCredential(ThirdPartyLoginDTO loginDTO) {
        // 这里实现支付宝授权验证逻辑
        // 通常需要调用支付宝开放平台接口验证access_token或auth_code
        log.info("验证支付宝登录凭证, identifier: {}", loginDTO.getIdentifier());
        // TODO: 实际项目中需要实现支付宝授权验证
        return true;
    }
}
