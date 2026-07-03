package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdMallProduct;
import org.jeecg.modules.member.mapper.FdMallProductMapper;
import org.jeecg.modules.member.service.IFdMallProductService;
import org.springframework.stereotype.Service;

@Service
public class FdMallProductServiceImpl extends ServiceImpl<FdMallProductMapper, FdMallProduct> implements IFdMallProductService {
}
