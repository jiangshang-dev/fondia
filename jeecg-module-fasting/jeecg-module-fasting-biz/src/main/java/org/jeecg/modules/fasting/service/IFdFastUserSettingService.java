package org.jeecg.modules.fasting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fasting.dto.FastSettingSaveDTO;
import org.jeecg.modules.fasting.entity.FdFastUserSetting;
import org.jeecg.modules.fasting.vo.FastSettingVO;

public interface IFdFastUserSettingService extends IService<FdFastUserSetting> {
    FastSettingVO getSetting(Long userId, String deviceId);

    FastSettingVO saveSetting(Long userId, String deviceId, FastSettingSaveDTO dto);
}
