package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.mapper.FdMallOrderMapper;
import org.jeecg.modules.member.service.IFdMallOrderService;
import org.springframework.stereotype.Service;

@Service
public class FdMallOrderServiceImpl extends ServiceImpl<FdMallOrderMapper, FdMallOrder> implements IFdMallOrderService {
}
