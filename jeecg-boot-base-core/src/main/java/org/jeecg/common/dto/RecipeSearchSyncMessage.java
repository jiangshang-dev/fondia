package org.jeecg.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜谱搜索索引同步消息（cuisine -> search via RabbitMQ）
 */
@Data
public class RecipeSearchSyncMessage implements Serializable {

    public static final String ACTION_SAVE = "SAVE";
    public static final String ACTION_DELETE = "DELETE";

    /** SAVE | DELETE */
    private String action;

    private String id;
    private String title;
    private String subtitle;
    private String coverImage;
    private String cuisineId;
    private String cuisineName;
    private String categoryId;
    private Integer status;
    private Integer difficulty;
    private Integer cookMinutes;
    private Integer likeCount;
    private List<String> tags;
    private List<String> ingredients;
    /** 聚合检索字段：标题/菜系/标签/食材 */
    private String searchText;
}
