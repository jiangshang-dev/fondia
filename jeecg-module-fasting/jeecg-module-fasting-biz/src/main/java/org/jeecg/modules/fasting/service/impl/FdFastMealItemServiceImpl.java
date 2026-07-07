package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fasting.entity.FdFastMealItem;
import org.jeecg.modules.fasting.mapper.FdFastMealItemMapper;
import org.jeecg.modules.fasting.service.IFdFastMealItemService;
import org.springframework.stereotype.Service;

@Service
public class FdFastMealItemServiceImpl extends ServiceImpl<FdFastMealItemMapper, FdFastMealItem>
        implements IFdFastMealItemService {
}
