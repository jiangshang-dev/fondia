package org.jeecg.modules.user.admin.service;

import org.jeecg.modules.user.admin.vo.dashboard.FdDashboardOverviewVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdRankItemVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdTrendPointVO;
import org.jeecg.modules.user.admin.vo.dashboard.FdWorkbenchVO;

import java.util.List;

public interface IFdDashboardService {

    FdDashboardOverviewVO getOverview();

    List<FdTrendPointVO> getUserTrend(int days);

    List<FdTrendPointVO> getMemberOrderTrend(int days);

    List<FdTrendPointVO> getMallOrderTrend(int days);

    List<FdRankItemVO> getTopMallProducts(int limit);

    FdWorkbenchVO getWorkbench();
}
