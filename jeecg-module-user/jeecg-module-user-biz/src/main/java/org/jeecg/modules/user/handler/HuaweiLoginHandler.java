package org.jeecg.modules.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.enums.IdentityTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 华为账号登录处理器（identifier 已在 huaweiLogin 中解析完成）
 */
@Slf4j
@Component
public class HuaweiLoginHandler implements ThirdPartyLoginHandler {

    @Override
    public String getIdentityType() {
        return IdentityTypeEnum.HUAWEI.getCode();
    }

    @Override
    public boolean verifyCredential(ThirdPartyLoginDTO loginDTO) {
        if (oConvertUtils.isEmpty(loginDTO.getIdentifier())) {
            log.warn("华为登录 identifier 为空");
            return false;
        }
        return true;
    }
}
