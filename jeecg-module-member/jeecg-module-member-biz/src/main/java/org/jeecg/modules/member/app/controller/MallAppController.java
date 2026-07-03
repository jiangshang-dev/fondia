package org.jeecg.modules.member.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.entity.FdMallProduct;
import org.jeecg.modules.member.service.IFdMallOrderService;
import org.jeecg.modules.member.service.IFdMallProductService;
import org.jeecg.modules.member.service.app.IMallRedeemAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App积分商城")
@RestController
@RequestMapping("/member/mall")
public class MallAppController {

    @Resource
    private IFdMallProductService productService;
    @Resource
    private IFdMallOrderService orderService;
    @Resource
    private IMallRedeemAppService redeemAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "商城商品列表")
    @GetMapping("/products")
    public Result<List<FdMallProduct>> products() {
        LambdaQueryWrapper<FdMallProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMallProduct::getStatus, 1).orderByAsc(FdMallProduct::getSortNo);
        return Result.OK(productService.list(wrapper));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/product/detail")
    public Result<FdMallProduct> detail(@RequestParam Long id) {
        return Result.OK(productService.getById(id));
    }

    @Operation(summary = "积分兑换")
    @PostMapping("/redeem")
    public Result<FdMallOrder> redeem(@RequestHeader("Authorization") String authorization,
                                      @RequestParam Long productId,
                                      @RequestParam(defaultValue = "1") Integer quantity,
                                      @RequestParam(required = false) Long addressId) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(redeemAppService.redeem(userId, productId, quantity, addressId));
    }

    @Operation(summary = "我的兑换订单")
    @GetMapping("/orders")
    public Result<IPage<FdMallOrder>> myOrders(@RequestHeader("Authorization") String authorization,
                                             @RequestParam(defaultValue = "1") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = authHelper.resolveUserId(authorization);
        LambdaQueryWrapper<FdMallOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMallOrder::getUserId, userId).orderByDesc(FdMallOrder::getCreateTime);
        return Result.OK(orderService.page(new Page<>(pageNo, pageSize), wrapper));
    }
}
