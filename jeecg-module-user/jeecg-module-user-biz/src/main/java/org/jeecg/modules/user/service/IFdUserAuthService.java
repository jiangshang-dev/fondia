package org.jeecg.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.user.entity.FdUserAuth;

/**
 * @Description: App用户第三方登录授权表 Service接口
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
public interface IFdUserAuthService extends IService<FdUserAuth> {

    /**
     * 根据登录渠道和唯一标识查询授权信息
     * @param identityType 登录渠道类型
     * @param identifier 第三方唯一标识
     * @return 用户授权信息
     */
    FdUserAuth getByIdentityTypeAndIdentifier(String identityType, String identifier);
}
