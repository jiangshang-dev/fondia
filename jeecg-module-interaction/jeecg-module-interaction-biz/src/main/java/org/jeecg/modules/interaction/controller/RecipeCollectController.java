package org.jeecg.modules.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.interaction.dto.RecipeInteractionDTO;
import org.jeecg.modules.interaction.entity.FdRecipeCollect;
import org.jeecg.modules.interaction.service.IFdRecipeCollectService;
import org.jeecg.modules.interaction.util.InteractionAuthHelper;
import org.jeecg.modules.interaction.vo.ToggleResultVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "菜谱收藏")
@RestController
@RequestMapping("/interaction/collect")
public class RecipeCollectController {

    @Resource
    private IFdRecipeCollectService collectService;

    @Resource
    private InteractionAuthHelper authHelper;

    @Operation(summary = "切换收藏状态")
    @PostMapping("/toggle")
    public Result<ToggleResultVO> toggle(@RequestHeader("Authorization") String authorization,
                                         @Validated @RequestBody RecipeInteractionDTO dto) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(collectService.toggleCollect(dto.getRecipeId(), userId));
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping("/my")
    public Result<IPage<FdRecipeCollect>> myCollects(@RequestHeader("Authorization") String authorization,
                                                      @RequestParam(defaultValue = "1") Integer pageNo,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(collectService.pageMyCollects(userId, pageNo, pageSize));
    }
}
