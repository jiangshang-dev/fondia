package org.jeecg.modules.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.media.entity.VideoConfigEntity;

/**
 * 视频业务配置DAO
 */
@Mapper
public interface VideoConfigMapper extends BaseMapper<VideoConfigEntity> {
	
}