package org.jeecg.modules.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FdUserMemberSyncMapper {

    @Update("UPDATE fd_user SET points = #{points}, check_in_days = #{checkInDays}, growth_value = growth_value + #{growthDelta}, update_time = #{updateTime} WHERE id = #{userId}")
    int syncAfterCheckin(@Param("userId") Long userId,
                         @Param("points") Integer points,
                         @Param("checkInDays") Integer checkInDays,
                         @Param("growthDelta") Integer growthDelta,
                         @Param("updateTime") Long updateTime);

    @Update("UPDATE fd_user SET points = #{points}, update_time = #{updateTime} WHERE id = #{userId}")
    int syncPointsOnly(@Param("userId") Long userId,
                       @Param("points") Integer points,
                       @Param("updateTime") Long updateTime);

    @Update("UPDATE fd_user SET member_status = #{memberStatus}, member_expire_time = #{expireTime}, member_plan_code = #{planCode}, update_time = #{updateTime} WHERE id = #{userId}")
    int syncMemberStatus(@Param("userId") Long userId,
                         @Param("memberStatus") Integer memberStatus,
                         @Param("expireTime") Long expireTime,
                         @Param("planCode") String planCode,
                         @Param("updateTime") Long updateTime);
}
