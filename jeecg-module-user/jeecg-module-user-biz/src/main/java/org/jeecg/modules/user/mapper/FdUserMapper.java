package org.jeecg.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.user.entity.FdUser;

/**
 * @Description: App用户表 Mapper接口
 * @Author: jeecg-boot
 * @Date:   2024-01-01
 * @Version: V1.0
 */
@Mapper
public interface FdUserMapper extends BaseMapper<FdUser> {
}
