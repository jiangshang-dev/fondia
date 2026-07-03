package org.jeecg.modules.user.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.admin.service.IFdDashboardService;
import org.jeecg.modules.user.admin.vo.dashboard.FdDashboardOverviewVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdRankItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdTrendPointVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdWorkbenchVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "饭典运营看板")
@RestController
@RequestMapping("/sys/fd/dashboard")
@RequiredArgsConstructor
public class FdDashboardAdminController {

    private final IFdDashboardService dashboardService;

    @Operation(summary = "运营概览(首页)")
    @GetMapping("/overview")
    public Result<FdDashboardOverviewVO> overview() {
        return Result.OK(dashboardService.getOverview());
    }

    @Operation(summary = "用户增长趋势")
    @GetMapping("/userTrend")
    public Result<List<FdTrendPointVO>> userTrend(
            @RequestParam(name = "days", defaultValue = "7") Integer days) {
        return Result.OK(dashboardService.getUserTrend(days));
    }

    @Operation(summary = "会员订单趋势")
    @GetMapping("/memberOrderTrend")
    public Result<List<FdTrendPointVO>> memberOrderTrend(
            @RequestParam(name = "days", defaultValue = "7") Integer days) {
        return Result.OK(dashboardService.getMemberOrderTrend(days));
    }

    @Operation(summary = "商城兑换趋势")
    @GetMapping("/mallOrderTrend")
    public Result<List<FdTrendPointVO>> mallOrderTrend(
            @RequestParam(name = "days", defaultValue = "7") Integer days) {
        return Result.OK(dashboardService.getMallOrderTrend(days));
    }

    @Operation(summary = "热门兑换商品排行")
    @GetMapping("/topMallProducts")
    public Result<List<FdRankItemVO>> topMallProducts(
            @RequestParam(name = "limit", defaultValue = "7") Integer limit) {
        return Result.OK(dashboardService.getTopMallProducts(limit));
    }

    @Operation(summary = "工作台数据")
    @GetMapping("/workbench")
    public Result<FdWorkbenchVO> workbench() {
        return Result.OK(dashboardService.getWorkbench());
    }
}
