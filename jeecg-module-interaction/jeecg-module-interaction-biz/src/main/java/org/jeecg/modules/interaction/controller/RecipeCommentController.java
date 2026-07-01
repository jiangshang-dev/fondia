package org.jeecg.modules.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.interaction.dto.AddCommentDTO;
import org.jeecg.modules.interaction.dto.CommentReactionDTO;
import org.jeecg.modules.interaction.dto.ReplyCommentDTO;
import org.jeecg.modules.interaction.entity.FdRecipeComment;
import org.jeecg.modules.interaction.service.IFdRecipeCommentReactionService;
import org.jeecg.modules.interaction.service.IFdRecipeCommentService;
import org.jeecg.modules.interaction.util.InteractionAuthHelper;
import org.jeecg.modules.interaction.vo.CommentReactionResultVO;
import org.jeecg.modules.interaction.vo.CommentTreePageVO;
import org.jeecg.modules.interaction.vo.CommentVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "菜谱评论")
@RestController
@RequestMapping("/interaction/comment")
public class RecipeCommentController {

    @Resource
    private IFdRecipeCommentService commentService;

    @Resource
    private IFdRecipeCommentReactionService reactionService;

    @Resource
    private InteractionAuthHelper authHelper;

    @Operation(summary = "分页查询一级评论（仅已发布）")
    @GetMapping("/list")
    public Result<IPage<FdRecipeComment>> list(@RequestParam Long recipeId,
                                               @RequestParam(defaultValue = "1") Integer pageNo,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(commentService.pageTopComments(recipeId, pageNo, pageSize));
    }

    @Operation(summary = "评论树（多级评论，含审核中仅自己可见）")
    @GetMapping("/tree")
    public Result<CommentTreePageVO> tree(@RequestParam Long recipeId,
                                          @RequestParam(defaultValue = "1") Integer pageNo,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = authHelper.resolveOptionalUserId(authorization);
        return Result.ok(commentService.pageCommentTree(recipeId, pageNo, pageSize, userId));
    }

    @Operation(summary = "查询评论回复列表（仅已发布）")
    @GetMapping("/replies")
    public Result<List<FdRecipeComment>> replies(@RequestParam Long rootId) {
        return Result.ok(commentService.listReplies(rootId));
    }

    @Operation(summary = "发表评论（阿里云审核，审核中仅自己可见）")
    @PostMapping("/add")
    public Result<CommentVO> add(@RequestHeader("Authorization") String authorization,
                                 @Validated @RequestBody AddCommentDTO dto,
                                 HttpServletRequest request) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(commentService.addComment(dto, userId, request.getRemoteAddr()));
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    public Result<CommentVO> reply(@RequestHeader("Authorization") String authorization,
                                   @Validated @RequestBody ReplyCommentDTO dto,
                                   HttpServletRequest request) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(commentService.replyComment(dto, userId, request.getRemoteAddr()));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<?> delete(@RequestHeader("Authorization") String authorization,
                            @PathVariable Long commentId) {
        Long userId = authHelper.resolveUserId(authorization);
        commentService.deleteComment(commentId, userId);
        return Result.ok("删除成功");
    }

    @Operation(summary = "评论点赞/点踩")
    @PostMapping("/reaction")
    public Result<CommentReactionResultVO> reaction(@RequestHeader("Authorization") String authorization,
                                                    @Validated @RequestBody CommentReactionDTO dto) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.ok(reactionService.toggleReaction(dto.getCommentId(), userId, dto.getReactionType()));
    }
}
