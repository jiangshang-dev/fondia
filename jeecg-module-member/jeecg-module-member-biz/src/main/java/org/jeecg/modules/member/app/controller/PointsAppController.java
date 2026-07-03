package org.jeecg.modules.member.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.entity.FdPointsLedger;
import org.jeecg.modules.member.entity.FdPointsTask;
import org.jeecg.modules.member.service.IFdPointsAccountService;
import org.jeecg.modules.member.service.IFdPointsLedgerService;
import org.jeecg.modules.member.service.IFdPointsTaskService;
import org.jeecg.modules.member.service.app.IPointsTaskAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App积分")
@RestController
@RequestMapping("/member/points")
public class PointsAppController {

    @Resource
    private IFdPointsAccountService accountService;
    @Resource
    private IFdPointsLedgerService ledgerService;
    @Resource
    private IFdPointsTaskService taskService;
    @Resource
    private IPointsTaskAppService pointsTaskAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "积分账户概览")
    @GetMapping("/summary")
    public Result<FdPointsAccount> summary(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        LambdaQueryWrapper<FdPointsAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsAccount::getUserId, userId);
        FdPointsAccount account = accountService.getOne(wrapper, false);
        if (account == null) {
            account = new FdPointsAccount();
            account.setUserId(userId);
            account.setBalance(0);
            account.setFrozen(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
        }
        return Result.OK(account);
    }

    @Operation(summary = "积分流水")
    @GetMapping("/ledger")
    public Result<IPage<FdPointsLedger>> ledger(@RequestHeader("Authorization") String authorization,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = authHelper.resolveUserId(authorization);
        LambdaQueryWrapper<FdPointsLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsLedger::getUserId, userId).orderByDesc(FdPointsLedger::getCreateTime);
        return Result.OK(ledgerService.page(new Page<>(pageNo, pageSize), wrapper));
    }

    @Operation(summary = "可完成任务列表")
    @GetMapping("/tasks")
    public Result<List<FdPointsTask>> tasks() {
        LambdaQueryWrapper<FdPointsTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsTask::getStatus, 1).orderByAsc(FdPointsTask::getSortNo);
        return Result.OK(taskService.list(wrapper));
    }

    @Operation(summary = "完成任务领积分")
    @PostMapping("/task/complete")
    public Result<Map<String, Object>> completeTask(@RequestHeader("Authorization") String authorization,
                                                     @RequestParam String taskCode,
                                                     @RequestParam(required = false) String bizRef) {
        Long userId = authHelper.resolveUserId(authorization);
        int balance = pointsTaskAppService.completeTask(userId, taskCode, bizRef);
        Map<String, Object> data = new HashMap<>();
        data.put("balanceAfter", balance);
        return Result.OK(data);
    }
}
