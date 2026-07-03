package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_mall_product")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "积分商城商品")
public class FdMallProduct extends FdMemberAdminEntity {

    @Schema(description = "商品编码")
    private String productCode;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "商品类型")
    private String productType;

    @Schema(description = "兑换积分")
    private Integer pointsPrice;

    @Schema(description = "市场价")
    private java.math.BigDecimal marketPrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "已兑换数")
    private Integer soldCount;

    @Schema(description = "每用户限兑")
    private Integer limitPerUser;

    @Schema(description = "详情")
    private String detailHtml;

    @Schema(description = "虚拟商品履约配置JSON")
    private String virtualConfigJson;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态")
    private Integer status;

}
