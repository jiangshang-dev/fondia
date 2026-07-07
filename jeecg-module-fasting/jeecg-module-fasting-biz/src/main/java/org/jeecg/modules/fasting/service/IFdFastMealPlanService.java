package org.jeecg.modules.fasting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fasting.entity.FdFastMealPlan;
import org.jeecg.modules.fasting.vo.FastMealDayVO;
import org.jeecg.modules.fasting.vo.FastWeeklyMealVO;

public interface IFdFastMealPlanService extends IService<FdFastMealPlan> {
    FastWeeklyMealVO getWeeklyMeals(String planCode);

    FastMealDayVO getTodayMeals(String planCode);

    void setDefaultPlan(Long planId);
}
