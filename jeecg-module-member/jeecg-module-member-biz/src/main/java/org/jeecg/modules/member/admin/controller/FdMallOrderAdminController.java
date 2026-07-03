package org.jeecg.modules.member.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.service.IFdMallOrderService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "商城兑换订单管理")
@RestController
@RequestMapping("/sys/fd/mall/order")
public class FdMallOrderAdminController extends JeecgController<FdMallOrder, IFdMallOrderService> {

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<IPage<FdMallOrder>> list(FdMallOrder query,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           HttpServletRequest req) {
        QueryWrapper<FdMallOrder> wrapper = QueryGenerator.initQueryWrapper(query, req.getParameterMap());
        wrapper.orderByDesc("create_time");
        Page<FdMallOrder> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, wrapper));
    }

    @Operation(summary = "详情")
    @GetMapping("/queryById")
    public Result<FdMallOrder> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }

    @AutoLog(value = "商城订单发货")
    @Operation(summary = "发货/完成")
    @PutMapping("/ship")
    public Result<String> ship(@RequestParam Long id,
                               @RequestParam(required = false) String logisticsNo,
                               @RequestParam(defaultValue = "1") Integer orderStatus) {
        FdMallOrder order = service.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setOrderStatus(orderStatus);
        if (logisticsNo != null) {
            order.setLogisticsNo(logisticsNo);
        }
        order.setUpdateTime(System.currentTimeMillis());
        service.updateById(order);
        return Result.OK("操作成功");
    }
}
