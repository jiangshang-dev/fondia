package org.jeecg.modules.member.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdCheckinConfig;
import org.jeecg.modules.member.entity.FdCheckinRecord;
import org.jeecg.modules.member.service.IFdCheckinConfigService;
import org.jeecg.modules.member.service.IFdCheckinRecordService;
import org.jeecg.modules.member.service.app.IMemberCheckinAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App签到")
@RestController
@RequestMapping("/member/checkin")
public class CheckinAppController {

    @Resource
    private IFdCheckinConfigService configService;
    @Resource
    private IFdCheckinRecordService recordService;
    @Resource
    private IMemberCheckinAppService checkinAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "7日签到规则")
    @GetMapping("/config")
    public Result<List<FdCheckinConfig>> config() {
        LambdaQueryWrapper<FdCheckinConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdCheckinConfig::getStatus, 1).orderByAsc(FdCheckinConfig::getDayIndex);
        return Result.OK(configService.list(wrapper));
    }

    @Operation(summary = "今日签到状态")
    @GetMapping("/today")
    public Result<Map<String, Object>> today(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<FdCheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdCheckinRecord::getUserId, userId).eq(FdCheckinRecord::getCheckinDate, today);
        FdCheckinRecord record = recordService.getOne(wrapper, false);
        Map<String, Object> data = new HashMap<>();
        data.put("checkedIn", record != null);
        data.put("record", record);
        return Result.OK(data);
    }

    @Operation(summary = "执行签到")
    @PostMapping("/do")
    public Result<Map<String, Object>> doCheckin(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(checkinAppService.doCheckin(userId));
    }
}
