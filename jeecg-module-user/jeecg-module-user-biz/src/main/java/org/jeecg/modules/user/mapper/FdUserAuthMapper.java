package org.jeecg.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.user.entity.FdUserAuth;

/**
 * @Description: App用户第三方登录授权表 Mapper接口
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Mapper
public interface FdUserAuthMapper extends BaseMapper<FdUserAuth> {
}
