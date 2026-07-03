package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.mapper.FdMemberPlanMapper;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.springframework.stereotype.Service;

@Service
public class FdMemberPlanServiceImpl extends ServiceImpl<FdMemberPlanMapper, FdMemberPlan> implements IFdMemberPlanService {
}
