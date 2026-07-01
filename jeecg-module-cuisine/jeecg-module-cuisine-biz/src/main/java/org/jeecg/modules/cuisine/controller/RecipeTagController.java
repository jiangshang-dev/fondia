package org.jeecg.modules.cuisine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.cuisine.service.IFdRecipeTagService;
import org.jeecg.modules.cuisine.vo.TagBriefVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "菜谱标签")
@RestController
@RequestMapping("/cuisine/tag")
public class RecipeTagController {

    @Resource
    private IFdRecipeTagService tagService;

    @Operation(summary = "标签列表")
    @GetMapping("/list")
    public Result<List<TagBriefVO>> list(@RequestParam(required = false) String tagType) {
        return Result.ok(tagService.listEnabledTags(tagType));
    }
}
