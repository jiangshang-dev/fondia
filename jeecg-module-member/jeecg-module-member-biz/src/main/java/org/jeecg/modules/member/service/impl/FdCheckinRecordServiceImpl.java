package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdCheckinRecord;
import org.jeecg.modules.member.mapper.FdCheckinRecordMapper;
import org.jeecg.modules.member.service.IFdCheckinRecordService;
import org.springframework.stereotype.Service;

@Service
public class FdCheckinRecordServiceImpl extends ServiceImpl<FdCheckinRecordMapper, FdCheckinRecord> implements IFdCheckinRecordService {
}
