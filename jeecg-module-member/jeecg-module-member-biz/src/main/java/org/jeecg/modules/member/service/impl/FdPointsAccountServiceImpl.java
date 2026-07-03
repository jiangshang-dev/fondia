package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.mapper.FdPointsAccountMapper;
import org.jeecg.modules.member.service.IFdPointsAccountService;
import org.springframework.stereotype.Service;

@Service
public class FdPointsAccountServiceImpl extends ServiceImpl<FdPointsAccountMapper, FdPointsAccount> implements IFdPointsAccountService {
}
