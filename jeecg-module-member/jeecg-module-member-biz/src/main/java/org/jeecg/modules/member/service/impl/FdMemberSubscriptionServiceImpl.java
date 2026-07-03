package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.jeecg.modules.member.mapper.FdMemberSubscriptionMapper;
import org.jeecg.modules.member.service.IFdMemberSubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class FdMemberSubscriptionServiceImpl extends ServiceImpl<FdMemberSubscriptionMapper, FdMemberSubscription> implements IFdMemberSubscriptionService {
}
