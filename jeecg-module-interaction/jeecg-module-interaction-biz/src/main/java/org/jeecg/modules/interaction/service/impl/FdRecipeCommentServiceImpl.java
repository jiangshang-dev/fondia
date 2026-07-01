package org.jeecg.modules.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.interaction.api.IUserBriefResolver;
import org.jeecg.modules.interaction.api.InteractionUserBrief;
import org.jeecg.modules.interaction.dto.AddCommentDTO;
import org.jeecg.modules.interaction.dto.ReplyCommentDTO;
import org.jeecg.modules.interaction.entity.FdRecipeComment;
import org.jeecg.modules.interaction.enums.CommentStatusEnum;
import org.jeecg.modules.interaction.mapper.FdRecipeCommentMapper;
import org.jeecg.modules.interaction.moderation.IContentModerationService;
import org.jeecg.modules.interaction.moderation.ModerationResult;
import org.jeecg.modules.interaction.service.IFdRecipeCommentReactionService;
import org.jeecg.modules.interaction.service.IFdRecipeCommentService;
import org.jeecg.modules.interaction.vo.CommentTreePageVO;
import org.jeecg.modules.interaction.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FdRecipeCommentServiceImpl extends ServiceImpl<FdRecipeCommentMapper, FdRecipeComment>
        implements IFdRecipeCommentService {

    @Resource
    private IContentModerationService contentModerationService;

    @Resource
    private IFdRecipeCommentReactionService reactionService;

    @Resource
    private IUserBriefResolver userBriefResolver;

    @Override
    public IPage<FdRecipeComment> pageTopComments(Long recipeId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdRecipeComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeComment::getRecipeId, recipeId)
                .eq(FdRecipeComment::getParentId, 0L)
                .eq(FdRecipeComment::getStatus, CommentStatusEnum.PUBLISHED.getCode())
                .orderByDesc(FdRecipeComment::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    @Override
    public List<FdRecipeComment> listReplies(Long rootId) {
        LambdaQueryWrapper<FdRecipeComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdRecipeComment::getRootId, rootId)
                .ne(FdRecipeComment::getParentId, 0L)
                .eq(FdRecipeComment::getStatus, CommentStatusEnum.PUBLISHED.getCode())
                .orderByAsc(FdRecipeComment::getCreateTime);
        return list(wrapper);
    }

    @Override
    public CommentTreePageVO pageCommentTree(Long recipeId, Integer pageNo, Integer pageSize, Long currentUserId) {
        LambdaQueryWrapper<FdRecipeComment> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(FdRecipeComment::getRecipeId, recipeId)
                .eq(FdRecipeComment::getParentId, 0L);
        applyVisibilityFilter(topWrapper, currentUserId);
        topWrapper.orderByDesc(FdRecipeComment::getCreateTime);

        IPage<FdRecipeComment> topPage = page(new Page<>(pageNo, pageSize), topWrapper);
        List<FdRecipeComment> topComments = topPage.getRecords();
        if (topComments.isEmpty()) {
            CommentTreePageVO empty = new CommentTreePageVO();
            empty.setRecords(new ArrayList<>());
            empty.setTotal(topPage.getTotal());
            empty.setCurrent(topPage.getCurrent());
            empty.setSize(topPage.getSize());
            empty.setPages(topPage.getPages());
            return empty;
        }

        List<Long> rootIds = topComments.stream().map(FdRecipeComment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<FdRecipeComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(FdRecipeComment::getRootId, rootIds)
                .ne(FdRecipeComment::getParentId, 0L);
        applyVisibilityFilter(replyWrapper, currentUserId);
        replyWrapper.orderByAsc(FdRecipeComment::getCreateTime);
        List<FdRecipeComment> allReplies = list(replyWrapper);

        Map<Long, List<FdRecipeComment>> repliesByRoot = allReplies.stream()
                .collect(Collectors.groupingBy(FdRecipeComment::getRootId, LinkedHashMap::new, Collectors.toList()));

        Set<Long> allCommentIds = new HashSet<>();
        Set<Long> allUserIds = new HashSet<>();
        collectIds(topComments, repliesByRoot, allCommentIds, allUserIds);
        Map<Long, InteractionUserBrief> userBriefMap = userBriefResolver.batchGet(allUserIds);
        Map<Long, Integer> myReactionMap = currentUserId == null
                ? Map.of()
                : reactionService.getUserReactions(currentUserId, allCommentIds);

        List<CommentVO> records = new ArrayList<>();
        for (FdRecipeComment top : topComments) {
            CommentVO vo = toCommentVO(top, currentUserId, userBriefMap, myReactionMap);
            List<FdRecipeComment> replies = repliesByRoot.getOrDefault(top.getId(), List.of());
            vo.setReplies(buildReplyTree(replies, top.getId(), currentUserId, userBriefMap, myReactionMap));
            records.add(vo);
        }

        CommentTreePageVO pageVO = new CommentTreePageVO();
        pageVO.setRecords(records);
        pageVO.setTotal(topPage.getTotal());
        pageVO.setCurrent(topPage.getCurrent());
        pageVO.setSize(topPage.getSize());
        pageVO.setPages(topPage.getPages());
        return pageVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(AddCommentDTO dto, Long userId, String ip) {
        Date now = new Date();
        FdRecipeComment comment = new FdRecipeComment();
        comment.setRecipeId(dto.getRecipeId());
        comment.setUserId(userId);
        comment.setParentId(0L);
        comment.setRootId(0L);
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        comment.setReplyCount(0);
        comment.setIp(ip);
        comment.setStatus(CommentStatusEnum.AUDITING.getCode());
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        save(comment);

        comment.setRootId(comment.getId());
        updateById(comment);

        applyModeration(comment);
        return toSingleCommentVO(comment, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO replyComment(ReplyCommentDTO dto, Long userId, String ip) {
        FdRecipeComment parent = getById(dto.getParentId());
        if (parent == null) {
            throw new JeecgBootException("父评论不存在");
        }
        if (!Objects.equals(parent.getStatus(), CommentStatusEnum.PUBLISHED.getCode())
                && !userId.equals(parent.getUserId())) {
            throw new JeecgBootException("该评论暂不可回复");
        }

        Date now = new Date();
        FdRecipeComment reply = new FdRecipeComment();
        reply.setRecipeId(parent.getRecipeId());
        reply.setUserId(userId);
        reply.setParentId(parent.getId());
        reply.setRootId(parent.getRootId() != null && parent.getRootId() > 0 ? parent.getRootId() : parent.getId());
        reply.setReplyUserId(parent.getUserId());
        reply.setContent(dto.getContent());
        reply.setLikeCount(0);
        reply.setDislikeCount(0);
        reply.setReplyCount(0);
        reply.setIp(ip);
        reply.setStatus(CommentStatusEnum.AUDITING.getCode());
        reply.setCreateTime(now);
        reply.setUpdateTime(now);
        save(reply);

        parent.setReplyCount((parent.getReplyCount() == null ? 0 : parent.getReplyCount()) + 1);
        parent.setUpdateTime(now);
        updateById(parent);

        applyModeration(reply);
        return toSingleCommentVO(reply, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        FdRecipeComment comment = getById(commentId);
        if (comment == null) {
            throw new JeecgBootException("评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new JeecgBootException("无权删除该评论");
        }
        removeById(commentId);
    }

    private void applyModeration(FdRecipeComment comment) {
        ModerationResult result = contentModerationService.moderate(comment.getContent());
        if (result.isPass()) {
            comment.setStatus(CommentStatusEnum.PUBLISHED.getCode());
        } else if (result.isReject()) {
            comment.setStatus(CommentStatusEnum.HIDDEN.getCode());
        }
        comment.setUpdateTime(new Date());
        updateById(comment);
    }

    private void applyVisibilityFilter(LambdaQueryWrapper<FdRecipeComment> wrapper, Long currentUserId) {
        wrapper.and(w -> {
            w.eq(FdRecipeComment::getStatus, CommentStatusEnum.PUBLISHED.getCode());
            if (currentUserId != null) {
                w.or(sub -> sub.eq(FdRecipeComment::getUserId, currentUserId)
                        .in(FdRecipeComment::getStatus,
                                CommentStatusEnum.AUDITING.getCode(),
                                CommentStatusEnum.HIDDEN.getCode()));
            }
        });
    }

    private void collectIds(List<FdRecipeComment> topComments,
                            Map<Long, List<FdRecipeComment>> repliesByRoot,
                            Set<Long> commentIds,
                            Set<Long> userIds) {
        for (FdRecipeComment top : topComments) {
            commentIds.add(top.getId());
            userIds.add(top.getUserId());
            if (top.getReplyUserId() != null) {
                userIds.add(top.getReplyUserId());
            }
            for (FdRecipeComment reply : repliesByRoot.getOrDefault(top.getId(), List.of())) {
                commentIds.add(reply.getId());
                userIds.add(reply.getUserId());
                if (reply.getReplyUserId() != null) {
                    userIds.add(reply.getReplyUserId());
                }
            }
        }
    }

    private List<CommentVO> buildReplyTree(List<FdRecipeComment> replies,
                                           Long parentId,
                                           Long currentUserId,
                                           Map<Long, InteractionUserBrief> userBriefMap,
                                           Map<Long, Integer> myReactionMap) {
        List<CommentVO> result = new ArrayList<>();
        for (FdRecipeComment reply : replies) {
            if (!Objects.equals(reply.getParentId(), parentId)) {
                continue;
            }
            CommentVO vo = toCommentVO(reply, currentUserId, userBriefMap, myReactionMap);
            vo.setReplies(buildReplyTree(replies, reply.getId(), currentUserId, userBriefMap, myReactionMap));
            result.add(vo);
        }
        return result;
    }

    private CommentVO toSingleCommentVO(FdRecipeComment comment, Long currentUserId) {
        Set<Long> userIds = new HashSet<>();
        userIds.add(comment.getUserId());
        if (comment.getReplyUserId() != null) {
            userIds.add(comment.getReplyUserId());
        }
        Map<Long, InteractionUserBrief> briefMap = userBriefResolver.batchGet(userIds);
        Map<Long, Integer> reactionMap = currentUserId == null
                ? Map.of()
                : reactionService.getUserReactions(currentUserId, List.of(comment.getId()));
        return toCommentVO(comment, currentUserId, briefMap, reactionMap);
    }

    private CommentVO toCommentVO(FdRecipeComment comment,
                                  Long currentUserId,
                                  Map<Long, InteractionUserBrief> userBriefMap,
                                  Map<Long, Integer> myReactionMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setRecipeId(comment.getRecipeId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setReplyUserId(comment.getReplyUserId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setDislikeCount(comment.getDislikeCount());
        vo.setReplyCount(comment.getReplyCount());
        vo.setStatus(comment.getStatus());
        vo.setStatusLabel(CommentStatusEnum.of(comment.getStatus()).getLabel());
        vo.setCreateTime(comment.getCreateTime());
        vo.setMyReaction(myReactionMap.get(comment.getId()));
        vo.setMine(currentUserId != null && currentUserId.equals(comment.getUserId()));

        InteractionUserBrief author = userBriefMap.get(comment.getUserId());
        if (author != null) {
            vo.setNickname(author.getNickname());
            vo.setAvatar(author.getAvatar());
        } else {
            vo.setNickname("用户" + comment.getUserId());
        }
        if (comment.getReplyUserId() != null) {
            InteractionUserBrief replyUser = userBriefMap.get(comment.getReplyUserId());
            vo.setReplyUserNickname(replyUser != null ? replyUser.getNickname() : "用户" + comment.getReplyUserId());
        }
        return vo;
    }
}
