package org.jeecg.modules.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.media.entity.VodConfigEntity;

/**
 * 阿里云VOD配置DAO
 */
@Mapper
public interface VodConfigMapper extends BaseMapper<VodConfigEntity> {
	
}