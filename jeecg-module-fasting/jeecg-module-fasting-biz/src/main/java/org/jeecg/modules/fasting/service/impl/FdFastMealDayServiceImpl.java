package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fasting.entity.FdFastMealDay;
import org.jeecg.modules.fasting.mapper.FdFastMealDayMapper;
import org.jeecg.modules.fasting.service.IFdFastMealDayService;
import org.springframework.stereotype.Service;

@Service
public class FdFastMealDayServiceImpl extends ServiceImpl<FdFastMealDayMapper, FdFastMealDay>
        implements IFdFastMealDayService {
}
