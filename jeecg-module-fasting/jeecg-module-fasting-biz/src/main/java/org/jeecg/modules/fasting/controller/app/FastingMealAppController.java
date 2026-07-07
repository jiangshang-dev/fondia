package org.jeecg.modules.fasting.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fasting.service.IFdFastMealPlanService;
import org.jeecg.modules.fasting.vo.FastMealDayVO;
import org.jeecg.modules.fasting.vo.FastWeeklyMealVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Fast16-减脂餐")
@RestController
@RequestMapping("/fasting/app/meals")
public class FastingMealAppController {

    @Resource
    private IFdFastMealPlanService mealPlanService;

    @Operation(summary = "获取周餐单（默认方案或指定 planCode）")
    @GetMapping("/weekly")
    public Result<FastWeeklyMealVO> weekly(@RequestParam(required = false) String planCode) {
        return Result.OK(mealPlanService.getWeeklyMeals(planCode));
    }

    @Operation(summary = "获取今日餐单")
    @GetMapping("/today")
    public Result<FastMealDayVO> today(@RequestParam(required = false) String planCode) {
        return Result.OK(mealPlanService.getTodayMeals(planCode));
    }
}
