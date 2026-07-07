package org.jeecg.modules.fasting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fasting.entity.FdFastPlan;

import java.util.List;

public interface IFdFastPlanService extends IService<FdFastPlan> {
    List<FdFastPlan> listEnabledPlans();
}
