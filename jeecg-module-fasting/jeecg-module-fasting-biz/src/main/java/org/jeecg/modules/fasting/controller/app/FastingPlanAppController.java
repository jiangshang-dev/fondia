package org.jeecg.modules.fasting.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fasting.service.IFdFastPlanService;
import org.jeecg.modules.fasting.vo.FastPlanVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Fast16-计划")
@RestController
@RequestMapping("/fasting/app/plans")
public class FastingPlanAppController {

    @Resource
    private IFdFastPlanService planService;

    @Operation(summary = "计划列表")
    @GetMapping
    public Result<List<FastPlanVO>> listPlans() {
        List<FastPlanVO> list = planService.listEnabledPlans().stream().map(plan -> {
            FastPlanVO vo = new FastPlanVO();
            BeanUtils.copyProperties(plan, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.OK(list);
    }
}
