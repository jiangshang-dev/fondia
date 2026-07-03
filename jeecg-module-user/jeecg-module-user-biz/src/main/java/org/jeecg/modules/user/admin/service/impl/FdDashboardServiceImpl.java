package org.jeecg.modules.user.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.user.admin.service.IFdDashboardService;
import org.jeecg.modules.user.admin.vo.dashboard.FdActivityItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdDashboardOverviewVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdRankItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdTodoItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdTrendPointVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdWorkbenchVO;
import org.jeecg.modules.user.mapper.FdDashboardMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FdDashboardServiceImpl implements IFdDashboardService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter RELATIVE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FdDashboardMapper dashboardMapper;

    @Override
    public FdDashboardOverviewVO getOverview() {
        long now = System.currentTimeMillis();
        long todayStart = startOfTodayMillis();
        long tomorrowStart = todayStart + TimeUnit.DAYS.toMillis(1);

        FdDashboardOverviewVO vo = new FdDashboardOverviewVO();
        vo.setTotalUsers(safeLong(dashboardMapper.countTotalUsers()));
        vo.setTodayNewUsers(safeLong(dashboardMapper.countUsersBetween(todayStart, tomorrowStart)));
        vo.setActiveMembers(safeLong(dashboardMapper.countActiveMembers(now)));
        vo.setTotalMemberSales(safeDecimal(dashboardMapper.sumMemberSales()));
        vo.setTodayMemberSales(safeDecimal(dashboardMapper.sumMemberSalesBetween(todayStart, tomorrowStart)));
        vo.setTotalPaidMemberOrders(safeLong(dashboardMapper.countPaidMemberOrders()));
        vo.setTodayPaidMemberOrders(safeLong(dashboardMapper.countPaidMemberOrdersBetween(todayStart, tomorrowStart)));
        vo.setTotalMallOrders(safeLong(dashboardMapper.countMallOrders()));
        vo.setPendingMallOrders(safeLong(dashboardMapper.countPendingMallOrders()));
        vo.setOnlineRecipes(safeLong(dashboardMapper.countOnlineRecipes()));
        vo.setPendingComments(safeLong(dashboardMapper.countPendingComments()));
        vo.setPendingFeedback(safeLong(dashboardMapper.countPendingFeedback()));
        vo.setPendingCsSessions(safeLong(dashboardMapper.countPendingCsSessions()));
        vo.setTotalPointsIssued(safeLong(dashboardMapper.sumPointsIssued()));
        return vo;
    }

    @Override
    public List<FdTrendPointVO> getUserTrend(int days) {
        return dashboardMapper.selectUserTrend(startMillis(days));
    }

    @Override
    public List<FdTrendPointVO> getMemberOrderTrend(int days) {
        return dashboardMapper.selectMemberOrderTrend(startMillis(days));
    }

    @Override
    public List<FdTrendPointVO> getMallOrderTrend(int days) {
        return dashboardMapper.selectMallOrderTrend(startMillis(days));
    }

    @Override
    public List<FdRankItemVO> getTopMallProducts(int limit) {
        return dashboardMapper.selectTopMallProducts(Math.max(1, Math.min(limit, 20)));
    }

    @Override
    public FdWorkbenchVO getWorkbench() {
        FdDashboardOverviewVO overview = getOverview();

        List<FdTodoItemVO> todos = buildTodos(overview);
        long todoTotal = todos.stream().mapToLong(t -> safeLong(t.getCount())).sum();

        List<FdActivityItemVO> activities = mergeActivities(8);
        activities.forEach(a -> a.setDate(formatRelativeTime(a.getTime())));

        FdWorkbenchVO vo = new FdWorkbenchVO();
        vo.setTodoTotal(todoTotal);
        vo.setTodoDone(0L);
        vo.setModuleCount(12L);
        vo.setTotalUsers(overview.getTotalUsers());
        vo.setTodos(todos);
        vo.setActivities(activities);
        return vo;
    }

    private List<FdTodoItemVO> buildTodos(FdDashboardOverviewVO overview) {
        List<FdTodoItemVO> list = new ArrayList<>();

        addTodo(list, "待审核评论", "有新的菜谱评论等待审核", overview.getPendingComments(),
                "/fondia/interaction/comment", "ant-design:message-outlined", "#1890ff");
        addTodo(list, "待处理反馈", "用户意见反馈待处理", overview.getPendingFeedback(),
                "/fondia/user/feedback", "ant-design:comment-outlined", "#722ed1");
        addTodo(list, "待发货兑换", "积分商城兑换订单待发货", overview.getPendingMallOrders(),
                "/fondia/mall/order", "ant-design:shopping-outlined", "#fa8c16");
        addTodo(list, "待接入客服", "客服会话等待人工接入", overview.getPendingCsSessions(),
                "/fondia/support/workbench", "ant-design:customer-service-outlined", "#13c2c2");

        list.sort(Comparator.comparing(FdTodoItemVO::getCount, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private void addTodo(List<FdTodoItemVO> list, String title, String desc, Long count,
                         String route, String icon, String color) {
        if (count == null || count <= 0) {
            return;
        }
        FdTodoItemVO item = new FdTodoItemVO();
        item.setTitle(title);
        item.setDesc(desc);
        item.setCount(count);
        item.setRoute(route);
        item.setIcon(icon);
        item.setColor(color);
        list.add(item);
    }

    private List<FdActivityItemVO> mergeActivities(int limit) {
        List<FdActivityItemVO> all = new ArrayList<>();
        all.addAll(dashboardMapper.selectRecentUsers(5));
        all.addAll(dashboardMapper.selectRecentMemberOrders(5));
        all.addAll(dashboardMapper.selectRecentMallOrders(5));
        all.addAll(dashboardMapper.selectRecentComments(5));
        all.addAll(dashboardMapper.selectRecentFeedback(5));
        all.sort(Comparator.comparing(FdActivityItemVO::getTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    private long startMillis(int days) {
        int safeDays = Math.max(1, Math.min(days, 90));
        LocalDate start = LocalDate.now(ZONE).minusDays(safeDays - 1L);
        return start.atStartOfDay(ZONE).toInstant().toEpochMilli();
    }

    private long startOfTodayMillis() {
        return LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant().toEpochMilli();
    }

    private String formatRelativeTime(Long time) {
        if (time == null || time <= 0) {
            return "";
        }
        long diffMs = System.currentTimeMillis() - time;
        if (diffMs < TimeUnit.MINUTES.toMillis(1)) {
            return "刚刚";
        }
        if (diffMs < TimeUnit.HOURS.toMillis(1)) {
            return (diffMs / TimeUnit.MINUTES.toMillis(1)) + " 分钟前";
        }
        if (diffMs < TimeUnit.DAYS.toMillis(1)) {
            return (diffMs / TimeUnit.HOURS.toMillis(1)) + " 小时前";
        }
        if (diffMs < TimeUnit.DAYS.toMillis(7)) {
            return ChronoUnit.DAYS.between(
                    Instant.ofEpochMilli(time).atZone(ZONE).toLocalDate(),
                    LocalDate.now(ZONE)) + " 天前";
        }
        return Instant.ofEpochMilli(time).atZone(ZONE).format(RELATIVE_FMT);
    }

    private long safeLong(Long val) {
        return val == null ? 0L : val;
    }

    private BigDecimal safeDecimal(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}
