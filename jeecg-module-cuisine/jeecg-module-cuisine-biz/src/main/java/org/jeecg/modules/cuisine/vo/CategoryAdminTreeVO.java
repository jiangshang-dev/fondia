package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "管理端菜系分类树节点")
public class CategoryAdminTreeVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "层级：1=菜系，2=子分类")
    private Integer categoryLevel;

    @Schema(description = "子分类类型：1=主食，2=菜")
    private Integer subType;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "特色标签JSON")
    private String featureTags;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "子节点")
    private List<CategoryAdminTreeVO> children = new ArrayList<>();
}
