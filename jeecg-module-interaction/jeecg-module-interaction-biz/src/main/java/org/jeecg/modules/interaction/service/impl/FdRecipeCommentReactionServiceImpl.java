package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.interaction.entity.FdRecipeComment;
import org.jeecg.modules.interaction.entity.FdRecipeCommentReaction;
import org.jeecg.modules.interaction.enums.CommentStatusEnum;
import org.jeecg.modules.interaction.enums.ReactionTypeEnum;
import org.jeecg.modules.interaction.mapper.FdRecipeCommentReactionMapper;
import org.jeecg.modules.interaction.service.IFdRecipeCommentReactionService;
import org.jeecg.modules.interaction.service.IFdRecipeCommentService;
import org.jeecg.modules.interaction.vo.CommentReactionResultVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FdRecipeCommentReactionServiceImpl extends ServiceImpl<FdRecipeCommentReactionMapper, FdRecipeCommentReaction>
        implements IFdRecipeCommentReactionService {

    private final IFdRecipeCommentService commentService;

    public FdRecipeCommentReactionServiceImpl(@Lazy IFdRecipeCommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentReactionResultVO toggleReaction(Long commentId, Long userId, Integer reactionType) {
        ReactionTypeEnum type = ReactionTypeEnum.of(reactionType);
        FdRecipeComment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new JeecgBootException("评论不存在");
        }
        if (!CommentStatusEnum.isPublished(comment.getStatus())) {
            throw new JeecgBootException("仅已发布的评论可以点赞或点踩");
        }

        LambdaQueryWrapper<FdRecipeCommentReaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCommentReaction::getCommentId, commentId)
                .eq(FdRecipeCommentReaction::getUserId, userId);
        FdRecipeCommentReaction existing = getOne(wrapper, false);
        Date now = new Date();
        Integer myReaction = null;

        if (existing == null) {
            FdRecipeCommentReaction reaction = new FdRecipeCommentReaction();
            reaction.setCommentId(commentId);
            reaction.setUserId(userId);
            reaction.setReactionType(type.getCode());
            reaction.setCreateTime(now);
            reaction.setUpdateTime(now);
            save(reaction);
            adjustCommentCount(comment, type, 1);
            myReaction = type.getCode();
        } else if (existing.getReactionType().equals(type.getCode())) {
            removeById(existing.getId());
            adjustCommentCount(comment, type, -1);
            myReaction = null;
        } else {
            ReactionTypeEnum oldType = ReactionTypeEnum.of(existing.getReactionType());
            existing.setReactionType(type.getCode());
            existing.setUpdateTime(now);
            updateById(existing);
            adjustCommentCount(comment, oldType, -1);
            adjustCommentCount(comment, type, 1);
            myReaction = type.getCode();
        }
        comment.setUpdateTime(now);
        commentService.updateById(comment);

        return CommentReactionResultVO.builder()
                .commentId(commentId)
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount())
                .myReaction(myReaction)
                .build();
    }

    @Override
    public Map<Long, Integer> getUserReactions(Long userId, Collection<Long> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<FdRecipeCommentReaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeCommentReaction::getUserId, userId)
                .in(FdRecipeCommentReaction::getCommentId, commentIds);
        List<FdRecipeCommentReaction> list = list(wrapper);
        Map<Long, Integer> map = new HashMap<>();
        for (FdRecipeCommentReaction reaction : list) {
            map.put(reaction.getCommentId(), reaction.getReactionType());
        }
        return map;
    }

    private void adjustCommentCount(FdRecipeComment comment, ReactionTypeEnum type, int delta) {
        if (type == ReactionTypeEnum.LIKE) {
            int count = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
            comment.setLikeCount(Math.max(0, count + delta));
        } else {
            int count = comment.getDislikeCount() == null ? 0 : comment.getDislikeCount();
            comment.setDislikeCount(Math.max(0, count + delta));
        }
    }
}
