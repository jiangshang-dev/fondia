package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.mapper.FdMemberOrderMapper;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.springframework.stereotype.Service;

@Service
public class FdMemberOrderServiceImpl extends ServiceImpl<FdMemberOrderMapper, FdMemberOrder> implements IFdMemberOrderService {
}
