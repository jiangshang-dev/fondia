package org.jeecg.modules.member.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.member.entity.FdCheckinRecord;
import org.jeecg.modules.member.service.IFdCheckinRecordService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "签到记录管理")
@RestController
@RequestMapping("/sys/fd/points/checkinRecord")
public class FdCheckinRecordAdminController extends JeecgController<FdCheckinRecord, IFdCheckinRecordService> {

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<IPage<FdCheckinRecord>> list(FdCheckinRecord query,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) {
        QueryWrapper<FdCheckinRecord> wrapper = QueryGenerator.initQueryWrapper(query, req.getParameterMap());
        wrapper.orderByDesc("create_time");
        Page<FdCheckinRecord> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, wrapper));
    }

    @Operation(summary = "详情")
    @GetMapping("/queryById")
    public Result<FdCheckinRecord> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }
}
