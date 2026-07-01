package org.jeecg.modules.interaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.interaction.dto.AddCommentDTO;
import org.jeecg.modules.interaction.dto.ReplyCommentDTO;
import org.jeecg.modules.interaction.entity.FdRecipeComment;
import org.jeecg.modules.interaction.vo.CommentTreePageVO;
import org.jeecg.modules.interaction.vo.CommentVO;

import java.util.List;

public interface IFdRecipeCommentService extends IService<FdRecipeComment> {

    IPage<FdRecipeComment> pageTopComments(Long recipeId, Integer pageNo, Integer pageSize);

    List<FdRecipeComment> listReplies(Long rootId);

    CommentTreePageVO pageCommentTree(Long recipeId, Integer pageNo, Integer pageSize, Long currentUserId);

    CommentVO addComment(AddCommentDTO dto, Long userId, String ip);

    CommentVO replyComment(ReplyCommentDTO dto, Long userId, String ip);

    void deleteComment(Long commentId, Long userId);
}
