package org.jeecg.modules.support.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TableName("fd_cs_faq")
@Schema(description = "客服FAQ")
public class FdCsFaq implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String question;
    private String answer;
    private String category;
    private Integer sortNo;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
