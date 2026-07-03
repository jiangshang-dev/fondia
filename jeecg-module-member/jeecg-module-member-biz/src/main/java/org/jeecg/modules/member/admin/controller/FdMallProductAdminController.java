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
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.member.entity.FdMallProduct;
import org.jeecg.modules.member.service.IFdMallProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

@Slf4j
@Tag(name = "商城商品管理")
@RestController
@RequestMapping("/sys/fd/mall/product")
public class FdMallProductAdminController extends JeecgController<FdMallProduct, IFdMallProductService> {

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<IPage<FdMallProduct>> list(FdMallProduct query,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) {
        QueryWrapper<FdMallProduct> wrapper = QueryGenerator.initQueryWrapper(query, req.getParameterMap());
        wrapper.orderByDesc("sort_no");
        Page<FdMallProduct> page = new Page<>(pageNo, pageSize);
        return Result.OK(service.page(page, wrapper));
    }

    @Operation(summary = "详情")
    @GetMapping("/queryById")
    public Result<FdMallProduct> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }

    @AutoLog(value = "新增商城商品管理", operateType = CommonConstant.OPERATE_TYPE_2)
    @Operation(summary = "新增")
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdMallProduct entity) {
        fillDefault(entity, true);
        service.save(entity);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑商城商品管理", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdMallProduct entity) {
        fillDefault(entity, false);
        service.updateById(entity);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除商城商品管理", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除商城商品管理", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }

    @AutoLog(value = "更新商城商品管理状态")
    @Operation(summary = "更新状态")
    @PutMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        FdMallProduct entity = service.getById(id);
        if (entity == null) {
            return Result.error("记录不存在");
        }
        entity.setStatus(status);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.OK("状态更新成功");
    }

    private void fillDefault(FdMallProduct entity, boolean isCreate) {
        Date now = new Date();
        if (isCreate && entity.getCreateTime() == null) {
            entity.setCreateTime(now);
        }
        entity.setUpdateTime(now);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
    }
}
