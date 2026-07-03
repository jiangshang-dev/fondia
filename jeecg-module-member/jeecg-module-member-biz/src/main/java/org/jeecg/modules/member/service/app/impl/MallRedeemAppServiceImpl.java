package org.jeecg.modules.member.service.app.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.entity.FdMallProduct;
import org.jeecg.modules.member.entity.FdUserAddress;
import org.jeecg.modules.member.service.IFdMallOrderService;
import org.jeecg.modules.member.service.IFdMallProductService;
import org.jeecg.modules.member.service.IFdUserAddressService;
import org.jeecg.modules.member.service.app.IMallRedeemAppService;
import org.jeecg.modules.member.service.core.PointsCoreService;
import org.jeecg.modules.member.service.core.VirtualFulfillmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MallRedeemAppServiceImpl implements IMallRedeemAppService {

    @Resource
    private IFdMallProductService productService;
    @Resource
    private IFdMallOrderService orderService;
    @Resource
    private IFdUserAddressService addressService;
    @Resource
    private PointsCoreService pointsCoreService;
    @Resource
    private VirtualFulfillmentService virtualFulfillmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FdMallOrder redeem(Long userId, Long productId, Integer quantity, Long addressId) {
        int qty = quantity == null || quantity < 1 ? 1 : quantity;
        FdMallProduct product = productService.getById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new JeecgBootException("商品不存在或已下架");
        }

        if (product.getLimitPerUser() != null && product.getLimitPerUser() > 0) {
            LambdaQueryWrapper<FdMallOrder> limitWrapper = new LambdaQueryWrapper<>();
            limitWrapper.eq(FdMallOrder::getUserId, userId)
                    .eq(FdMallOrder::getProductId, productId)
                    .ne(FdMallOrder::getOrderStatus, 3);
            long bought = orderService.count(limitWrapper);
            if (bought + qty > product.getLimitPerUser()) {
                throw new JeecgBootException("超出每用户兑换上限");
            }
        }

        if (product.getStock() != null && product.getStock() >= 0 && product.getStock() < qty) {
            throw new JeecgBootException("库存不足");
        }

        int totalPoints = product.getPointsPrice() * qty;
        if (pointsCoreService.getBalance(userId) < totalPoints) {
            throw new JeecgBootException("积分不足");
        }

        long now = System.currentTimeMillis();
        FdMallOrder order = new FdMallOrder();
        order.setOrderNo("MO" + now + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductCode(product.getProductCode());
        order.setProductName(product.getName());
        order.setProductType(product.getProductType());
        order.setPointsCost(totalPoints);
        order.setQuantity(qty);
        order.setCreateTime(now);
        order.setUpdateTime(now);

        String type = product.getProductType() != null ? product.getProductType().toUpperCase() : "";
        boolean isPhysical = "PHYSICAL".equals(type);
        boolean isVirtual = "VIRTUAL".equals(type) || "COUPON".equals(type);

        if (isPhysical) {
            if (addressId == null) {
                throw new JeecgBootException("请选择收货地址");
            }
            FdUserAddress address = addressService.getById(addressId);
            if (address == null || !userId.equals(address.getUserId())) {
                throw new JeecgBootException("收货地址无效");
            }
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(address.fullAddress());
            order.setOrderStatus(0);
        } else if (isVirtual) {
            order.setOrderStatus(2);
        } else {
            throw new JeecgBootException("不支持的商品类型");
        }

        pointsCoreService.deductPoints(userId, totalPoints, "MALL_REDEEM", order.getOrderNo(), "兑换-" + product.getName());
        orderService.save(order);

        if (isVirtual) {
            var fulfillResult = virtualFulfillmentService.fulfill(userId, product.getVirtualConfigJson());
            order.setVirtualPayload(JSONObject.toJSONString(fulfillResult));
            order.setUpdateTime(System.currentTimeMillis());
            orderService.updateById(order);
        }

        if (product.getStock() != null && product.getStock() > 0) {
            product.setStock(product.getStock() - qty);
        }
        product.setSoldCount((product.getSoldCount() == null ? 0 : product.getSoldCount()) + qty);
        productService.updateById(product);

        return order;
    }
}
