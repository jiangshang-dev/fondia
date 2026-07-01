package org.jeecg.modules.interaction.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "评论展示对象")
public class CommentVO {

    private Long id;
    private Long recipeId;
    private Long userId;
    private Long parentId;
    private Long rootId;
    private Long replyUserId;

    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer replyCount;
    private Integer status;
    private String statusLabel;

    private String nickname;
    private String avatar;
    private String replyUserNickname;

    /** 当前用户反应：1点赞 2点踩 null无 */
    private Integer myReaction;

    private Boolean mine;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "子回复（多级评论）")
    private List<CommentVO> replies = new ArrayList<>();
}
