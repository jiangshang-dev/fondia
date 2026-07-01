package org.jeecg.modules.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.interaction.entity.FdRecipeCommentReaction;
import org.jeecg.modules.interaction.vo.CommentReactionResultVO;

import java.util.Collection;
import java.util.Map;

public interface IFdRecipeCommentReactionService extends IService<FdRecipeCommentReaction> {

    CommentReactionResultVO toggleReaction(Long commentId, Long userId, Integer reactionType);

    Map<Long, Integer> getUserReactions(Long userId, Collection<Long> commentIds);
}
