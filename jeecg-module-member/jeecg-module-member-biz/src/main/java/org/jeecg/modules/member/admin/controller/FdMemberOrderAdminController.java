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
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "会员订单管理")
@RestController
@RequestMapping("/sys/fd/member/order")
public class FdMemberOrderAdminController extends JeecgController<FdMemberOrder, IFdMemberOrderService> {

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<IPage<FdMemberOrder>> list(FdMemberOrder query,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) {
        QueryWrapper<FdMemberOrder> wrapper = QueryGenerator.initQueryWrapper(query, req.getParameterMap());
        wrapper.orderByDesc("create_time");
        Page<FdMemberOrder> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, wrapper));
    }

    @Operation(summary = "详情")
    @GetMapping("/queryById")
    public Result<FdMemberOrder> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }
}
