package org.jeecg.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("fd_member_plan")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "会员套餐")
public class FdMemberPlan extends FdMemberAdminEntity {

    @Schema(description = "套餐编码")
    private String planCode;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "有效天数")
    private Integer durationDays;

    @Schema(description = "售价")
    private java.math.BigDecimal price;

    @Schema(description = "原价")
    private java.math.BigDecimal originalPrice;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "适用平台")
    private String platformScope;

    @Schema(description = "权益JSON")
    private String benefitsJson;

    @Schema(description = "赠送积分")
    private Integer bonusPoints;

    @Schema(description = "赠送成长值")
    private Integer bonusGrowth;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "状态：0下架 1上架")
    private Integer status;

    @Schema(description = "Apple IAP 商品 ID")
    private String appleProductId;

    @Schema(description = "Google Play 商品 ID")
    private String googleProductId;

    @Schema(description = "AI营养咨询每日次数，0=不限，NULL=沿用免费额度")
    private Integer aiNutritionDailyLimit;

}
