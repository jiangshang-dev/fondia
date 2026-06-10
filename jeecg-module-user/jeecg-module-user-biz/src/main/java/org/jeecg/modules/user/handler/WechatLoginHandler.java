package org.jeecg.modules.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.enums.IdentityTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 微信登录处理器
 */
@Slf4j
@Component
public class WechatLoginHandler implements ThirdPartyLoginHandler {

    @Override
    public String getIdentityType() {
        return IdentityTypeEnum.WECHAT.getCode();
    }

    @Override
    public boolean verifyCredential(ThirdPartyLoginDTO loginDTO) {
        // 这里实现微信授权验证逻辑
        // 通常需要调用微信开放平台接口验证code
        log.info("验证微信登录凭证, identifier: {}", loginDTO.getIdentifier());
        // TODO: 实际项目中需要实现微信授权验证
        return true;
    }
}
