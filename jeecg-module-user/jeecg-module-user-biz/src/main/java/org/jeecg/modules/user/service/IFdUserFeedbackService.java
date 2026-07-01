package org.jeecg.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.user.dto.FeedbackSubmitDTO;
import org.jeecg.modules.user.entity.FdUserFeedback;
import org.jeecg.modules.user.vo.FeedbackAdminVO;
import org.jeecg.modules.user.vo.UserInfoVO;

import com.baomidou.mybatisplus.core.metadata.IPage;

public interface IFdUserFeedbackService extends IService<FdUserFeedback> {

    void submitFeedback(UserInfoVO user, FeedbackSubmitDTO dto);

    IPage<FeedbackAdminVO> pageForAdmin(FdUserFeedback query, Integer pageNo, Integer pageSize);

    FeedbackAdminVO getAdminDetail(Long id);

    void updateStatus(Long id, Integer status);
}
