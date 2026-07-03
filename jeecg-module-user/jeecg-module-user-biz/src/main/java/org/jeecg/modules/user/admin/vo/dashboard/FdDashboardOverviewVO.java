package org.jeecg.modules.user.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "饭典运营概览")
public class FdDashboardOverviewVO {

    @Schema(description = "App用户总数")
    private Long totalUsers;

    @Schema(description = "今日新增用户")
    private Long todayNewUsers;

    @Schema(description = "生效中会员数")
    private Long activeMembers;

    @Schema(description = "会员累计销售额")
    private BigDecimal totalMemberSales;

    @Schema(description = "今日会员销售额")
    private BigDecimal todayMemberSales;

    @Schema(description = "会员累计订单数(已支付)")
    private Long totalPaidMemberOrders;

    @Schema(description = "今日会员订单数(已支付)")
    private Long todayPaidMemberOrders;

    @Schema(description = "商城兑换订单总数")
    private Long totalMallOrders;

    @Schema(description = "待发货兑换订单")
    private Long pendingMallOrders;

    @Schema(description = "上架菜谱数")
    private Long onlineRecipes;

    @Schema(description = "待审核评论")
    private Long pendingComments;

    @Schema(description = "待处理反馈")
    private Long pendingFeedback;

    @Schema(description = "待接入客服会话")
    private Long pendingCsSessions;

    @Schema(description = "积分累计发放")
    private Long totalPointsIssued;
}
