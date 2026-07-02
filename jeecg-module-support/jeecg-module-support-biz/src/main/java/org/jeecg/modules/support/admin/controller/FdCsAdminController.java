package org.jeecg.modules.support.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.modules.support.entity.FdCsConfig;
import org.jeecg.modules.support.entity.FdCsSession;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.service.IFdCsSessionService;
import org.jeecg.modules.support.vo.CsMessageVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "客服配置与会话管理")
@RestController
@RequestMapping("/sys/fd/cs")
public class FdCsAdminController {

    @Resource
    private IFdCsConfigService configService;

    @Resource
    private IFdCsSessionService sessionService;

    @Operation(summary = "获取客服全局配置")
    @GetMapping("/config")
    public Result<FdCsConfig> getConfig() {
        return Result.OK(configService.getConfigEntity());
    }

    @AutoLog(value = "保存客服配置", operateType = CommonConstant.OPERATE_TYPE_3)
    @PostMapping("/config/save")
    public Result<String> saveConfig(@RequestBody FdCsConfig config) {
        configService.saveOrUpdateConfig(config);
        return Result.OK("保存成功");
    }

    @Operation(summary = "会话分页列表")
    @GetMapping("/session/list")
    public Result<IPage<FdCsSession>> sessionList(FdCsSession query,
                                                  @RequestParam(defaultValue = "1") Integer pageNo,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.OK(sessionService.pageForAdmin(query, pageNo, pageSize));
    }

    @AutoLog(value = "客服接入会话", operateType = CommonConstant.OPERATE_TYPE_3)
    @PostMapping("/session/accept")
    public Result<String> accept(@RequestParam String id, @RequestParam String agentId) {
        sessionService.acceptSession(id, agentId);
        return Result.OK("接入成功");
    }

    @AutoLog(value = "客服关闭会话", operateType = CommonConstant.OPERATE_TYPE_3)
    @PostMapping("/session/close")
    public Result<String> close(@RequestParam String id) {
        sessionService.adminCloseSession(id);
        return Result.OK("会话已关闭");
    }

    @Operation(summary = "会话消息列表")
    @GetMapping("/session/messages")
    public Result<List<CsMessageVO>> messages(@RequestParam Long sessionId,
                                              @RequestParam(defaultValue = "1") Integer pageNo,
                                              @RequestParam(defaultValue = "100") Integer pageSize) {
        // 管理端不做用户校验，agent已登录
        FdCsSession session = sessionService.getById(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        return Result.OK(sessionService.listMessagesForAdmin(sessionId, pageNo, pageSize));
    }
}
