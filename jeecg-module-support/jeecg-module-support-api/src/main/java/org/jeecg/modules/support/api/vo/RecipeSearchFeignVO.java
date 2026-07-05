package org.jeecg.modules.support.api.vo;

import lombok.Data;

/**
 * 菜谱搜索 Feign 结果项（与 search 服务 RecipeSearchItemVO 字段对齐）
 */
@Data
public class RecipeSearchFeignVO {

    private String id;
    private String title;
    private String subtitle;
    private String coverImage;
    private Integer cookMinutes;
}
