package org.jeecg.modules.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.support.entity.FdCsQuickEntry;
import org.jeecg.modules.support.mapper.FdCsQuickEntryMapper;
import org.jeecg.modules.support.service.IFdCsQuickEntryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FdCsQuickEntryServiceImpl extends ServiceImpl<FdCsQuickEntryMapper, FdCsQuickEntry> implements IFdCsQuickEntryService {

    @Override
    public List<FdCsQuickEntry> listEnabled() {
        return list(new LambdaQueryWrapper<FdCsQuickEntry>()
                .eq(FdCsQuickEntry::getStatus, 1)
                .orderByAsc(FdCsQuickEntry::getSortNo)
                .orderByDesc(FdCsQuickEntry::getCreateTime));
    }
}
