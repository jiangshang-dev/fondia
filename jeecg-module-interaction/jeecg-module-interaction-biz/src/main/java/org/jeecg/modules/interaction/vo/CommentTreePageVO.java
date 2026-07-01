package org.jeecg.modules.interaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "评论树分页")
public class CommentTreePageVO {

    private List<CommentVO> records = new ArrayList<>();
    private long total;
    private long current;
    private long size;
    private long pages;
}
