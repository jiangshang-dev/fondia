package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdHomeBanner;
import org.jeecg.modules.cuisine.vo.HomeBannerVO;

import java.util.List;

public interface IFdHomeBannerService extends IService<FdHomeBanner> {

    List<HomeBannerVO> listEnabledBanners();
}
