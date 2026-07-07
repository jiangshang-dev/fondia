package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.fasting.entity.FdFastMealDay;
import org.jeecg.modules.fasting.entity.FdFastMealItem;
import org.jeecg.modules.fasting.entity.FdFastMealPlan;
import org.jeecg.modules.fasting.enums.MealTypeEnum;
import org.jeecg.modules.fasting.mapper.FdFastMealPlanMapper;
import org.jeecg.modules.fasting.service.IFdFastMealDayService;
import org.jeecg.modules.fasting.service.IFdFastMealItemService;
import org.jeecg.modules.fasting.service.IFdFastMealPlanService;
import org.jeecg.modules.fasting.vo.FastMealDayVO;
import org.jeecg.modules.fasting.vo.FastMealItemVO;
import org.jeecg.modules.fasting.vo.FastWeeklyMealVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FdFastMealPlanServiceImpl extends ServiceImpl<FdFastMealPlanMapper, FdFastMealPlan>
        implements IFdFastMealPlanService {

    private static final List<String> MEAL_ORDER = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

    @Resource
    private IFdFastMealDayService mealDayService;
    @Resource
    private IFdFastMealItemService mealItemService;

    @Override
    public FastWeeklyMealVO getWeeklyMeals(String planCode) {
        FdFastMealPlan plan = resolvePlan(planCode);
        List<FdFastMealDay> days = mealDayService.list(new LambdaQueryWrapper<FdFastMealDay>()
                .eq(FdFastMealDay::getPlanId, plan.getId())
                .orderByAsc(FdFastMealDay::getDayOfWeek));
        if (days.isEmpty()) {
            throw new JeecgBootException("该方案尚未配置每日餐单");
        }
        List<Long> dayIds = days.stream().map(FdFastMealDay::getId).collect(Collectors.toList());
        List<FdFastMealItem> items = mealItemService.list(new LambdaQueryWrapper<FdFastMealItem>()
                .in(FdFastMealItem::getDayId, dayIds)
                .eq(FdFastMealItem::getStatus, 1)
                .orderByAsc(FdFastMealItem::getSortOrder));
        Map<Long, List<FdFastMealItem>> itemMap = items.stream()
                .collect(Collectors.groupingBy(FdFastMealItem::getDayId));

        FastWeeklyMealVO vo = new FastWeeklyMealVO();
        vo.setPlanCode(plan.getCode());
        vo.setPlanName(plan.getName());
        vo.setDescription(plan.getDescription());
        vo.setTargetKcalMin(plan.getTargetKcalMin());
        vo.setTargetKcalMax(plan.getTargetKcalMax());
        List<FastMealDayVO> dayVOList = new ArrayList<>();
        for (FdFastMealDay day : days) {
            dayVOList.add(toDayVO(day, itemMap.getOrDefault(day.getId(), List.of())));
        }
        vo.setDays(dayVOList);
        return vo;
    }

    @Override
    public FastMealDayVO getTodayMeals(String planCode) {
        int dayOfWeek = resolveTodayDayOfWeek();
        FdFastMealPlan plan = resolvePlan(planCode);
        FdFastMealDay day = mealDayService.getOne(new LambdaQueryWrapper<FdFastMealDay>()
                .eq(FdFastMealDay::getPlanId, plan.getId())
                .eq(FdFastMealDay::getDayOfWeek, dayOfWeek)
                .last("LIMIT 1"));
        if (day == null) {
            throw new JeecgBootException("今日餐单未配置");
        }
        List<FdFastMealItem> items = mealItemService.list(new LambdaQueryWrapper<FdFastMealItem>()
                .eq(FdFastMealItem::getDayId, day.getId())
                .eq(FdFastMealItem::getStatus, 1)
                .orderByAsc(FdFastMealItem::getSortOrder));
        return toDayVO(day, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultPlan(Long planId) {
        FdFastMealPlan plan = getById(planId);
        if (plan == null) {
            throw new JeecgBootException("方案不存在");
        }
        update(new LambdaUpdateWrapper<FdFastMealPlan>().set(FdFastMealPlan::getIsDefault, 0));
        plan.setIsDefault(1);
        updateById(plan);
    }

    private FdFastMealPlan resolvePlan(String planCode) {
        LambdaQueryWrapper<FdFastMealPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdFastMealPlan::getStatus, 1);
        if (oConvertUtils.isNotEmpty(planCode)) {
            wrapper.eq(FdFastMealPlan::getCode, planCode);
        } else {
            wrapper.eq(FdFastMealPlan::getIsDefault, 1);
        }
        wrapper.last("LIMIT 1");
        FdFastMealPlan plan = getOne(wrapper);
        if (plan == null && oConvertUtils.isEmpty(planCode)) {
            plan = getOne(new LambdaQueryWrapper<FdFastMealPlan>()
                    .eq(FdFastMealPlan::getStatus, 1)
                    .orderByAsc(FdFastMealPlan::getSortOrder)
                    .last("LIMIT 1"));
        }
        if (plan == null) {
            throw new JeecgBootException("未找到可用的减脂餐方案");
        }
        return plan;
    }

    private int resolveTodayDayOfWeek() {
        int jsDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        // Calendar: SUNDAY=1 … SATURDAY=7 → 周一=1 … 周日=7
        return jsDay == Calendar.SUNDAY ? 7 : jsDay - 1;
    }

    private FastMealDayVO toDayVO(FdFastMealDay day, List<FdFastMealItem> items) {
        FastMealDayVO vo = new FastMealDayVO();
        vo.setDayOfWeek(day.getDayOfWeek());
        vo.setDayLabel(day.getDayLabel());
        vo.setDailyKcal(day.getDailyKcal());
        vo.setTip(day.getTip());
        List<FastMealItemVO> mealVOs = items.stream()
                .sorted(Comparator.comparingInt(i -> MEAL_ORDER.indexOf(i.getMealType())))
                .map(this::toItemVO)
                .collect(Collectors.toList());
        vo.setMeals(mealVOs);
        return vo;
    }

    private FastMealItemVO toItemVO(FdFastMealItem item) {
        FastMealItemVO vo = new FastMealItemVO();
        vo.setMealType(item.getMealType());
        vo.setMealTypeLabel(resolveMealLabel(item.getMealType()));
        vo.setName(item.getName());
        vo.setDescription(item.getDescription());
        vo.setKcal(item.getKcal());
        if (oConvertUtils.isNotEmpty(item.getTags())) {
            vo.setTags(Arrays.stream(item.getTags().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        } else {
            vo.setTags(List.of());
        }
        return vo;
    }

    private String resolveMealLabel(String mealType) {
        for (MealTypeEnum e : MealTypeEnum.values()) {
            if (e.getCode().equals(mealType)) {
                return e.getLabel();
            }
        }
        return mealType;
    }
}
