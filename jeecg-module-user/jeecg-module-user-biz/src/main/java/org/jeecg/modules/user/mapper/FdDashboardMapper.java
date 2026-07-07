package org.jeecg.modules.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.user.admin.vo.dashboard.FdActivityItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdRankItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdTrendPointVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fast16 运营看板（仅查询轻断食/用户相关表，不再依赖 Fondia 商城/会员/菜谱表）
 */
@Mapper
public interface FdDashboardMapper {

    @Select("SELECT COUNT(*) FROM fd_user WHERE status <> -1")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM fd_user WHERE status <> -1 AND create_time >= #{startTime} AND create_time < #{endTime}")
    Long countUsersBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    /** 进行中断食/进食会话 */
    @Select("SELECT COUNT(*) FROM fd_fast_session WHERE end_time IS NULL")
    Long countActiveFastingSessions();

    /** 累计完成会话（已结束） */
    @Select("SELECT COUNT(*) FROM fd_fast_session WHERE end_time IS NOT NULL")
    Long countCompletedSessions();

    @Select("SELECT COUNT(*) FROM fd_fast_session WHERE end_time IS NOT NULL AND end_time >= FROM_UNIXTIME(#{startTime} / 1000) AND end_time < FROM_UNIXTIME(#{endTime} / 1000)")
    Long countCompletedSessionsBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    /** 餐单方案数 */
    @Select("SELECT COUNT(*) FROM fd_fast_meal_plan WHERE status = 1")
    Long countMealPlans();

    /** 餐次条目数 */
    @Select("SELECT COUNT(*) FROM fd_fast_meal_item WHERE status = 1")
    Long countMealItems();

    /** 断食计划模板数 */
    @Select("SELECT COUNT(*) FROM fd_fast_plan WHERE status = 1")
    Long countFastingPlans();

    @Select("SELECT 0")
    Long countPendingComments();

    @Select("SELECT COUNT(*) FROM fd_user_feedback WHERE status = 0")
    Long countPendingFeedback();

    @Select("SELECT 0")
    Long countPendingCsSessions();

    @Select("SELECT COUNT(*) FROM fd_fast_ai_message")
    Long countAiMessages();

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
            SELECT DATE(end_time) AS date,
                   COUNT(*) AS count,
                   NULL AS amount
            FROM fd_fast_session
            WHERE end_time IS NOT NULL AND end_time >= FROM_UNIXTIME(#{startTime} / 1000)
            GROUP BY DATE(end_time)
            ORDER BY date
            """)
    List<FdTrendPointVO> selectSessionTrend(@Param("startTime") Long startTime);

    @Select("""
            SELECT DATE(create_time) AS date,
                   COUNT(*) AS count,
                   NULL AS amount
            FROM fd_fast_ai_message
            WHERE create_time >= FROM_UNIXTIME(#{startTime} / 1000)
            GROUP BY DATE(create_time)
            ORDER BY date
            """)
    List<FdTrendPointVO> selectAiMessageTrend(@Param("startTime") Long startTime);

    @Select("""
            SELECT p.name AS name, COUNT(i.id) AS value
            FROM fd_fast_meal_plan p
            LEFT JOIN fd_fast_meal_item i ON i.plan_id = p.id AND i.status = 1
            WHERE p.status = 1
            GROUP BY p.id, p.name
            ORDER BY value DESC
            LIMIT #{limit}
            """)
    List<FdRankItemVO> selectTopMealPlans(@Param("limit") int limit);

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
            SELECT CONCAT('会话 ', s.id) AS name,
                   CONCAT(s.phase, ' · 计划 ', IFNULL(s.plan_code, '-')) AS `desc`,
                   UNIX_TIMESTAMP(s.start_time) * 1000 AS time,
                   'dynamic-avatar-2|svg' AS avatar
            FROM fd_fast_session s
            ORDER BY s.start_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentSessions(@Param("limit") int limit);

    @Select("""
            SELECT CONCAT('AI #', m.id) AS name,
                   CONCAT(m.role, '：', LEFT(m.content, 40)) AS `desc`,
                   UNIX_TIMESTAMP(m.create_time) * 1000 AS time,
                   'dynamic-avatar-3|svg' AS avatar
            FROM fd_fast_ai_message m
            ORDER BY m.create_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentAiMessages(@Param("limit") int limit);

    @Select("""
            SELECT CONCAT('反馈 #', f.id) AS name,
                   CONCAT('用户反馈：', LEFT(f.content, 40)) AS `desc`,
                   f.create_time AS time,
                   'dynamic-avatar-4|svg' AS avatar
            FROM fd_user_feedback f
            ORDER BY f.create_time DESC
            LIMIT #{limit}
            """)
    List<FdActivityItemVO> selectRecentFeedback(@Param("limit") int limit);

    /** 兼容旧 Service 字段映射：会员相关置 0 */
    @Select("SELECT 0")
    Long countActiveMembers(@Param("now") Long now);

    @Select("SELECT 0")
    BigDecimal sumMemberSales();

    @Select("SELECT 0")
    BigDecimal sumMemberSalesBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Select("SELECT 0")
    Long countPaidMemberOrders();

    @Select("SELECT 0")
    Long countPaidMemberOrdersBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Select("SELECT COUNT(*) FROM fd_fast_meal_item WHERE status = 1")
    Long countMallOrders();

    @Select("SELECT 0")
    Long countPendingMallOrders();

    @Select("SELECT COUNT(*) FROM fd_fast_meal_item WHERE status = 1")
    Long countOnlineRecipes();

    @Select("SELECT COUNT(*) FROM fd_fast_ai_message")
    Long sumPointsIssued();

    default List<FdTrendPointVO> selectMemberOrderTrend(Long startTime) {
        return selectSessionTrend(startTime);
    }

    default List<FdTrendPointVO> selectMallOrderTrend(Long startTime) {
        return selectAiMessageTrend(startTime);
    }

    default List<FdRankItemVO> selectTopMallProducts(int limit) {
        return selectTopMealPlans(limit);
    }

    default List<FdActivityItemVO> selectRecentMemberOrders(int limit) {
        return selectRecentSessions(limit);
    }

    default List<FdActivityItemVO> selectRecentMallOrders(int limit) {
        return selectRecentAiMessages(limit);
    }

    default List<FdActivityItemVO> selectRecentComments(int limit) {
        return List.of();
    }
}
