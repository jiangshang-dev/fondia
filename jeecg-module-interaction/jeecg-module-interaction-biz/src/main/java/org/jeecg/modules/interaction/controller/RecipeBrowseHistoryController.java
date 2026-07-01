package org.jeecg.modules.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.interaction.dto.RecipeInteractionDTO;
import org.jeecg.modules.interaction.entity.FdRecipeBrowseHistory;
import org.jeecg.modules.interaction.service.IFdRecipeBrowseHistoryService;
import org.jeecg.modules.interaction.util.InteractionAuthHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "浏览记录")
@RestController
@RequestMapping("/interaction/browse")
public class RecipeBrowseHistoryController {

    @Resource
    private IFdRecipeBrowseHistoryService browseHistoryService;

    @Resource
    private InteractionAuthHelper authHelper;

    @Operation(summary = "记录浏览")
    @PostMapping("/record")
    public Result<?> record(@RequestHeader("Authorization") String authorization,
                            @Validated @RequestBody RecipeInteractionDTO dto) {
        Long userId = authHelper.resolveUserId(authorization);
        browseHistoryService.recordBrowse(dto.getRecipeId(), userId);
        return Result.ok("记录成功");
    }

    @Operation(summary = "我的浏览记录")
    @GetMapping("/my")
    public Result<IPage<FdRecipeBrowseHistory>> myHistory(@RequestHeader("Authorization") String authorization,
                                                          @RequestParam(defaultValue = "1") Integer pageNo,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(browseHistoryService.pageMyHistory(userId, pageNo, pageSize));
    }
}
