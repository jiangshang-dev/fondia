package org.jeecg.modules.interaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.interaction.dto.RecipeInteractionDTO;
import org.jeecg.modules.interaction.service.IFdRecipeLikeService;
import org.jeecg.modules.interaction.util.InteractionAuthHelper;
import org.jeecg.modules.interaction.vo.ToggleResultVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "菜谱点赞")
@RestController
@RequestMapping("/interaction/like")
public class RecipeLikeController {

    @Resource
    private IFdRecipeLikeService likeService;

    @Resource
    private InteractionAuthHelper authHelper;

    @Operation(summary = "切换点赞状态")
    @PostMapping("/toggle")
    public Result<ToggleResultVO> toggle(@RequestHeader("Authorization") String authorization,
                                         @Validated @RequestBody RecipeInteractionDTO dto) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(likeService.toggleLike(dto.getRecipeId(), userId));
    }

    @Operation(summary = "是否已点赞")
    @GetMapping("/status")
    public Result<Boolean> status(@RequestHeader("Authorization") String authorization,
                                  @RequestParam Long recipeId) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(likeService.isLiked(recipeId, userId));
    }
}
