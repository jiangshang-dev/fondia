package org.jeecg.modules.interaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "评论点赞点踩结果")
public class CommentReactionResultVO {

    private Long commentId;
    private Integer likeCount;
    private Integer dislikeCount;
    /** 1点赞 2点踩 null取消 */
    private Integer myReaction;
}
