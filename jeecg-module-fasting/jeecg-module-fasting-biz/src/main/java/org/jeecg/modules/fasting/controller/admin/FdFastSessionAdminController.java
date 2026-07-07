package org.jeecg.modules.fasting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fasting.entity.FdFastSession;
import org.jeecg.modules.fasting.service.IFdFastSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "断食会话记录")
@RestController
@RequestMapping("/sys/fasting/session")
public class FdFastSessionAdminController extends JeecgController<FdFastSession, IFdFastSessionService> {

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<IPage<FdFastSession>> list(FdFastSession session,
                                             @RequestParam(defaultValue = "1") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             HttpServletRequest req) {
        QueryWrapper<FdFastSession> qw = QueryGenerator.initQueryWrapper(session, req.getParameterMap());
        qw.orderByDesc("start_time");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), qw));
    }
}
