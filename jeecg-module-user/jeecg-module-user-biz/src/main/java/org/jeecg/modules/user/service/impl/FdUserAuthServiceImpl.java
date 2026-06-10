package org.jeecg.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.user.entity.FdUserAuth;
import org.jeecg.modules.user.mapper.FdUserAuthMapper;
import org.jeecg.modules.user.service.IFdUserAuthService;
import org.springframework.stereotype.Service;

/**
 * @Description: App用户第三方登录授权表 Service实现
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Slf4j
@Service
public class FdUserAuthServiceImpl extends ServiceImpl<FdUserAuthMapper, FdUserAuth> implements IFdUserAuthService {

    @Override
    public FdUserAuth getByIdentityTypeAndIdentifier(String identityType, String identifier) {
        LambdaQueryWrapper<FdUserAuth> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FdUserAuth::getIdentityType, identityType);
        queryWrapper.eq(FdUserAuth::getIdentifier, identifier);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
