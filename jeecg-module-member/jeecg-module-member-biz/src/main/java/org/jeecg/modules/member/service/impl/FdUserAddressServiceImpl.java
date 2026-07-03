package org.jeecg.modules.member.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.member.entity.FdUserAddress;
import org.jeecg.modules.member.mapper.FdUserAddressMapper;
import org.jeecg.modules.member.service.IFdUserAddressService;
import org.springframework.stereotype.Service;
@Service
public class FdUserAddressServiceImpl extends ServiceImpl<FdUserAddressMapper, FdUserAddress> implements IFdUserAddressService {}
