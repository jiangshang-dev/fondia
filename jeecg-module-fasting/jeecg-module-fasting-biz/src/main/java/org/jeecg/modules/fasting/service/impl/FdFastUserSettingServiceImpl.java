package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.fasting.dto.FastSettingSaveDTO;
import org.jeecg.modules.fasting.entity.FdFastUserSetting;
import org.jeecg.modules.fasting.mapper.FdFastUserSettingMapper;
import org.jeecg.modules.fasting.service.IFdFastUserSettingService;
import org.jeecg.modules.fasting.vo.FastSettingVO;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FdFastUserSettingServiceImpl extends ServiceImpl<FdFastUserSettingMapper, FdFastUserSetting>
        implements IFdFastUserSettingService {

    @Override
    public FastSettingVO getSetting(Long userId, String deviceId) {
        FdFastUserSetting setting = findSetting(userId, deviceId);
        if (setting == null) {
            FastSettingVO vo = new FastSettingVO();
            vo.setPlanCode("16_8");
            vo.setEatStartTime("12:00:00");
            vo.setHapticEnabled(1);
            vo.setLocale("zh_CN");
            return vo;
        }
        return toVO(setting);
    }

    @Override
    public FastSettingVO saveSetting(Long userId, String deviceId, FastSettingSaveDTO dto) {
        if (userId == null && oConvertUtils.isEmpty(deviceId)) {
            throw new JeecgBootException("缺少用户或设备标识");
        }
        FdFastUserSetting setting = findSetting(userId, deviceId);
        Date now = new Date();
        if (setting == null) {
            setting = new FdFastUserSetting();
            setting.setUserId(userId);
            setting.setDeviceId(deviceId);
            setting.setCreateTime(now);
        }
        setting.setPlanCode(dto.getPlanCode());
        if (oConvertUtils.isNotEmpty(dto.getEatStartTime())) {
            setting.setEatStartTime(normalizeTime(dto.getEatStartTime()));
        }
        if (dto.getHapticEnabled() != null) {
            setting.setHapticEnabled(dto.getHapticEnabled());
        }
        if (oConvertUtils.isNotEmpty(dto.getLocale())) {
            setting.setLocale(dto.getLocale());
        }
        setting.setUpdateTime(now);
        saveOrUpdate(setting);
        return toVO(setting);
    }

    private FdFastUserSetting findSetting(Long userId, String deviceId) {
        LambdaQueryWrapper<FdFastUserSetting> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(FdFastUserSetting::getUserId, userId);
        } else {
            wrapper.eq(FdFastUserSetting::getDeviceId, deviceId);
        }
        wrapper.last("LIMIT 1");
        return getOne(wrapper);
    }

    private FastSettingVO toVO(FdFastUserSetting setting) {
        FastSettingVO vo = new FastSettingVO();
        vo.setPlanCode(setting.getPlanCode());
        vo.setEatStartTime(setting.getEatStartTime());
        vo.setHapticEnabled(setting.getHapticEnabled());
        vo.setLocale(setting.getLocale());
        return vo;
    }

    private String normalizeTime(String time) {
        if (time.length() == 5) {
            return time + ":00";
        }
        return time;
    }
}
