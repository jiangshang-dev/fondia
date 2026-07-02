package org.jeecg.modules.support.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.support.entity.FdCsFaq;

import java.util.List;

public interface IFdCsFaqService extends IService<FdCsFaq> {

    List<FdCsFaq> listEnabled();
}
