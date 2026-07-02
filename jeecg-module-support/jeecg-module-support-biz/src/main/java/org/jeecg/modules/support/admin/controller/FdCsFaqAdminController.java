package org.jeecg.modules.support.admin.controller;

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
import org.jeecg.modules.support.entity.FdCsConfig;
import org.jeecg.modules.support.entity.FdCsFaq;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.service.IFdCsFaqService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@Tag(name = "客服FAQ管理")
@RestController
@RequestMapping("/sys/fd/cs/faq")
public class FdCsFaqAdminController extends JeecgController<FdCsFaq, IFdCsFaqService> {

    @Operation(summary = "FAQ分页列表")
    @GetMapping("/list")
    public Result<IPage<FdCsFaq>> list(FdCsFaq faq,
                                       @RequestParam(defaultValue = "1") Integer pageNo,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<FdCsFaq> queryWrapper = QueryGenerator.initQueryWrapper(faq, req.getParameterMap());
        queryWrapper.orderByAsc("sort_no").orderByDesc("create_time");
        return Result.OK(service.page(new Page<>(pageNo, pageSize), queryWrapper));
    }

    @AutoLog(value = "新增客服FAQ", operateType = CommonConstant.OPERATE_TYPE_2)
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdCsFaq faq) {
        long now = System.currentTimeMillis();
        faq.setCreateTime(now);
        faq.setUpdateTime(now);
        if (faq.getStatus() == null) {
            faq.setStatus(1);
        }
        service.save(faq);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑客服FAQ", operateType = CommonConstant.OPERATE_TYPE_3)
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdCsFaq faq) {
        faq.setUpdateTime(System.currentTimeMillis());
        service.updateById(faq);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除客服FAQ", operateType = CommonConstant.OPERATE_TYPE_4)
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
