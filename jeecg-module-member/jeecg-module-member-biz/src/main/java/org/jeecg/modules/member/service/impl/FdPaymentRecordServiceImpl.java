package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdPaymentRecord;
import org.jeecg.modules.member.mapper.FdPaymentRecordMapper;
import org.jeecg.modules.member.service.IFdPaymentRecordService;
import org.springframework.stereotype.Service;

@Service
public class FdPaymentRecordServiceImpl extends ServiceImpl<FdPaymentRecordMapper, FdPaymentRecord> implements IFdPaymentRecordService {
}
