package org.jeecg.modules.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 菜谱浏览记录表
 */
@Data
@TableName("fd_recipe_browse_history")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜谱浏览记录表")
public class FdRecipeBrowseHistory extends FdInteractionBaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "菜谱ID")
    private Long recipeId;

    @Schema(description = "最近浏览时间")
    private Date browseTime;
}
