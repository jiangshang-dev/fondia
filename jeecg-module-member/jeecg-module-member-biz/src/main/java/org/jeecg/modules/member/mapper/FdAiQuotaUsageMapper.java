package org.jeecg.modules.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.member.entity.FdAiQuotaUsage;

public interface FdAiQuotaUsageMapper extends BaseMapper<FdAiQuotaUsage> {

    @Update("UPDATE fd_ai_quota_usage SET used_count = used_count + 1, update_time = NOW() "
            + "WHERE user_id = #{userId} AND feature_code = #{featureCode} AND usage_date = #{usageDate} "
            + "AND used_count < #{limit}")
    int incrementIfUnderLimit(@Param("userId") Long userId,
                              @Param("featureCode") String featureCode,
                              @Param("usageDate") Integer usageDate,
                              @Param("limit") int limit);
}
