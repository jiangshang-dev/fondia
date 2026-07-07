package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fasting.entity.FdFastPlan;
import org.jeecg.modules.fasting.mapper.FdFastPlanMapper;
import org.jeecg.modules.fasting.service.IFdFastPlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FdFastPlanServiceImpl extends ServiceImpl<FdFastPlanMapper, FdFastPlan> implements IFdFastPlanService {

    @Override
    public List<FdFastPlan> listEnabledPlans() {
        return list(new LambdaQueryWrapper<FdFastPlan>()
                .eq(FdFastPlan::getStatus, 1)
                .orderByAsc(FdFastPlan::getSortOrder));
    }
}
