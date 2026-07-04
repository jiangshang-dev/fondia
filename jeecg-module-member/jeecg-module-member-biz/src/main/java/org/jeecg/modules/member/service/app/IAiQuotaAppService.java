package org.jeecg.modules.member.service.app;

import org.jeecg.modules.member.vo.AiQuotaAdminSummaryVO;
import org.jeecg.modules.member.vo.AiQuotaStatusVO;

import java.util.List;

public interface IAiQuotaAppService {

    AiQuotaStatusVO getStatus(Long userId, String featureCode);

    AiQuotaStatusVO consume(Long userId, String featureCode);

    AiQuotaAdminSummaryVO getAdminSummary(String featureCode);

    void saveAdminConfig(String featureCode, Integer freeDailyLimit, Integer status, String remark);

    void savePlanLimits(List<AiQuotaAdminSummaryVO.PlanQuotaItemVO> plans);
}
