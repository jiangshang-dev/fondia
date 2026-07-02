package org.jeecg.modules.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.support.entity.FdCsFaq;
import org.jeecg.modules.support.mapper.FdCsFaqMapper;
import org.jeecg.modules.support.service.IFdCsFaqService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FdCsFaqServiceImpl extends ServiceImpl<FdCsFaqMapper, FdCsFaq> implements IFdCsFaqService {

    @Override
    public List<FdCsFaq> listEnabled() {
        return list(new LambdaQueryWrapper<FdCsFaq>()
                .eq(FdCsFaq::getStatus, 1)
                .orderByAsc(FdCsFaq::getSortNo)
                .orderByDesc(FdCsFaq::getCreateTime));
    }
}
