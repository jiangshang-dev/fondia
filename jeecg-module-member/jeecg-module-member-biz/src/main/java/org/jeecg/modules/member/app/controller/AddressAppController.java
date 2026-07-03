package org.jeecg.modules.member.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdUserAddress;
import org.jeecg.modules.member.service.IFdUserAddressService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "App收货地址")
@RestController
@RequestMapping("/member/address")
public class AddressAppController {

    @Resource
    private IFdUserAddressService addressService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "地址列表")
    @GetMapping("/list")
    public Result<List<FdUserAddress>> list(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        LambdaQueryWrapper<FdUserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdUserAddress::getUserId, userId).orderByDesc(FdUserAddress::getIsDefault).orderByDesc(FdUserAddress::getUpdateTime);
        return Result.OK(addressService.list(wrapper));
    }

    @Operation(summary = "新增/编辑地址")
    @PostMapping("/save")
    public Result<FdUserAddress> save(@RequestHeader("Authorization") String authorization,
                                      @RequestBody FdUserAddress address) {
        Long userId = authHelper.resolveUserId(authorization);
        long now = System.currentTimeMillis();
        address.setUserId(userId);
        address.setUpdateTime(now);
        if (address.getId() == null) {
            address.setCreateTime(now);
        }
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            clearDefault(userId);
        }
        addressService.saveOrUpdate(address);
        return Result.OK(address);
    }

    @Operation(summary = "设为默认")
    @PutMapping("/default")
    public Result<String> setDefault(@RequestHeader("Authorization") String authorization,
                                     @RequestParam Long id) {
        Long userId = authHelper.resolveUserId(authorization);
        FdUserAddress address = addressService.getById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new JeecgBootException("地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(1);
        address.setUpdateTime(System.currentTimeMillis());
        addressService.updateById(address);
        return Result.OK("设置成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestHeader("Authorization") String authorization,
                                 @RequestParam Long id) {
        Long userId = authHelper.resolveUserId(authorization);
        FdUserAddress address = addressService.getById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new JeecgBootException("地址不存在");
        }
        addressService.removeById(id);
        return Result.OK("删除成功");
    }

    private void clearDefault(Long userId) {
        LambdaQueryWrapper<FdUserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdUserAddress::getUserId, userId).eq(FdUserAddress::getIsDefault, 1);
        List<FdUserAddress> list = addressService.list(wrapper);
        for (FdUserAddress item : list) {
            item.setIsDefault(0);
            addressService.updateById(item);
        }
    }
}
