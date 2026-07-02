package org.jeecg.modules.support.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.support.entity.FdCsQuickEntry;

import java.util.List;

public interface IFdCsQuickEntryService extends IService<FdCsQuickEntry> {

    List<FdCsQuickEntry> listEnabled();
}
