package org.jeecg.modules.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.user.admin.vo.dashboard.FdActivityItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdRankItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdTrendPointVO;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FdDashboardMapper {

    @Select("SELECT COUNT(*) FROM fd_user WHERE status <> -1")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM fd_user WHERE status <> -1 AND create_time >= #{startTime} AND create_time < #{endTime}")
    Long countUsersBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Select("SELECT COUNT(*) FROM fd_member_subscription WHERE status = 1 AND expire_time > #{now}")
    Long countActiveMembers(@Param("now") Long now);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM fd_member_order WHERE pay_status = 1")
    BigDecimal sumMemberSales();

    @Select("SELECT IFNULL(SUM(amount), 0) FROM fd_member_order WHERE pay_status = 1 AND paid_time >= #{startTime} AND paid_time < #{endTime}")
    BigDecimal sumMemberSalesBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Select("SELECT COUNT(*) FROM fd_member_order WHERE pay_status = 1")
    Long countPaidMemberOrders();

    @Select("SELECT COUNT(*) FROM fd_member_order WHERE pay_status = 1 AND paid_time >= #{startTime} AND paid_time < #{endTime}")
    Long countPaidMemberOrdersBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Select("SELECT COUNT(*) FROM fd_mall_order")
    Long countMallOrders();

    @Select("SELECT COUNT(*) FROM fd_mall_order WHERE order_status = 0")
    Long countPendingMallOrders();

    @Select("SELECT COUNT(*) FROM fd_recipe WHERE status = 1 AND del_flag = 0")
    Long countOnlineRecipes();

    @Select("SELECT COUNT(*) FROM fd_recipe_comment WHERE status = 2 AND del_flag = 0")
    Long countPendingComments();

    @Select("SELECT COUNT(*) FROM fd_user_feedback WHERE status = 0")
    Long countPendingFeedback();

    @Select("SELECT COUNT(*) FROM fd_cs_session WHERE status IN ('WAITING', 'CHATTING')")
    Long countPendingCsSessions();

    @Select("SELECT IFNULL(SUM(CASE WHEN change_amount > 0 THEN change_amount ELSE 0 END), 0) FROM fd_points_ledger")
    Long sumPointsIssued();

    @Select("""
            SELECT DATE(FROM_UNIXTIME(create_time / 1000)) AS date,
                   COUNT(*) AS count,
                   NULL AS amount
            FROM fd_user
            WHERE status <> -1 AND create_time >= #{startTime}
            GROUP BY DATE(FROM_UNIXTIME(create_time / 1000))
            ORDER BY date
            """)
    List<FdTrendPointVO> selectUserTrend(@Param("startTime") Long startTime);

    @Select("""
            SELECT DATE(FROM_UNIXTIME(paid_time / 1000)) AS date,
                   COUNT(*) AS count,
                   IFNULL(SUM(amount), 0) AS amount
            FROM fd_member_order
            WHERE pay_status = 1 AND paid_time >= #{startTime}
            GROUP BY DATE(FROM_UNIXTIME(paid_time / 1000))
            ORDER BY date
            """)
    List<FdTrendPointVO> selectMemberOrderTrend(@Param("startTime") Long startTime);

    @Select("""
            SELECT DATE(FROM_UNIXTIME(create_time / 1000)) AS date,
                   COUNT(*) AS count,
                   IFNULL(SUM(points_cost * quantity), 0) AS amount
            FROM fd_mall_order
            WHERE create_time >= #{startTime}
            GROUP BY DATE(FROM_UNIXTIME(create_time / 1000))
            ORDER BY date
            """)
    List<FdTrendPointVO> selectMallOrderTrend(@Param("startTime") Long startTime);

    @Select("""
            SELECT product_name AS name, COUNT(*) AS value
            FROM fd_mall_order
            GROUP BY product_id, product_name
            ORDER BY value DESC
            LIMIT #{limit}
            """)
    List<FdRankItemVO> selectTopMallProducts(@Param("limit") int limit);

    @Select("""
            SELECT IFNULL(u.nickname, u.username) AS name,
                   CONCAT('新用户注册：', IFNULL(u.nickname, u.username)) AS `desc`,
                   u.create_time AS time,
                   'dynamic-avatar-1|svg' AS avatar
            FROM fd_user u
            WHERE u.status <> -1
            ORDER BY u.create_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentUsers(@Param("limit") int limit);

    @Select("""
            SELECT CONCAT('订单 ', o.order_no) AS name,
                   CONCAT('会员订单支付 ¥', o.amount, ' · ', o.plan_name) AS `desc`,
                   o.paid_time AS time,
                   'dynamic-avatar-2|svg' AS avatar
            FROM fd_member_order o
            WHERE o.pay_status = 1 AND o.paid_time IS NOT NULL
            ORDER BY o.paid_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentMemberOrders(@Param("limit") int limit);

    @Select("""
            SELECT CONCAT('兑换 ', o.order_no) AS name,
                   CONCAT('积分兑换 ', o.product_name, ' · ', o.points_cost * o.quantity, ' 积分') AS `desc`,
                   o.create_time AS time,
                   'dynamic-avatar-3|svg' AS avatar
            FROM fd_mall_order o
            ORDER BY o.create_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentMallOrders(@Param("limit") int limit);

    @Select("""
            SELECT CONCAT('评论 #', c.id) AS name,
                   CONCAT('用户评论菜谱：', LEFT(c.content, 40)) AS `desc`,
                   UNIX_TIMESTAMP(c.create_time) * 1000 AS time,
                   'dynamic-avatar-4|svg' AS avatar
            FROM fd_recipe_comment c
            WHERE c.del_flag = 0
            ORDER BY c.create_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentComments(@Param("limit") int limit);

    @Select("""
            SELECT CONCAT('反馈 #', f.id) AS name,
                   CONCAT('用户反馈：', LEFT(f.content, 40)) AS `desc`,
                   f.create_time AS time,
                   'dynamic-avatar-5|svg' AS avatar
            FROM fd_user_feedback f
            ORDER BY f.create_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentFeedback(@Param("limit") int limit);
}
