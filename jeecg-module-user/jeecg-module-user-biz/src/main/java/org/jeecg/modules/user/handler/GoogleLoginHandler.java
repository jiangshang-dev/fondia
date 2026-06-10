package org.jeecg.modules.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.enums.IdentityTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Google登录处理器
 */
@Slf4j
@Component
public class GoogleLoginHandler implements ThirdPartyLoginHandler {

    @Override
    public String getIdentityType() {
        return IdentityTypeEnum.GOOGLE.getCode();
    }

    @Override
    public boolean verifyCredential(ThirdPartyLoginDTO loginDTO) {
        // 这里实现Google授权验证逻辑
        // 通常需要调用Google API验证id_token或access_token
        log.info("验证Google登录凭证, identifier: {}", loginDTO.getIdentifier());
        // TODO: 实际项目中需要实现Google授权验证
        return true;
    }
}
