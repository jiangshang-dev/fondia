package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdCheckinConfig;
import org.jeecg.modules.member.mapper.FdCheckinConfigMapper;
import org.jeecg.modules.member.service.IFdCheckinConfigService;
import org.springframework.stereotype.Service;

@Service
public class FdCheckinConfigServiceImpl extends ServiceImpl<FdCheckinConfigMapper, FdCheckinConfig> implements IFdCheckinConfigService {
}
