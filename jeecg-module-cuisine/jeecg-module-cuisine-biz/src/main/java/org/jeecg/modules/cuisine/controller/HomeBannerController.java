package org.jeecg.modules.cuisine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.cuisine.service.IFdHomeBannerService;
import org.jeecg.modules.cuisine.vo.HomeBannerVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "首页轮播")
@RestController
@RequestMapping("/cuisine/banner")
public class HomeBannerController {

    @Resource
    private IFdHomeBannerService bannerService;

    @Operation(summary = "首页轮播图列表")
    @GetMapping("/list")
    public Result<List<HomeBannerVO>> list() {
        return Result.ok(bannerService.listEnabledBanners());
    }
}
