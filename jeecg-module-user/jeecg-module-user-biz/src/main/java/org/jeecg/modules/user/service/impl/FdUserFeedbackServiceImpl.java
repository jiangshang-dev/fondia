package org.jeecg.modules.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.dto.FeedbackSubmitDTO;
import org.jeecg.modules.user.entity.FdUser;
import org.jeecg.modules.user.entity.FdUserFeedback;
import org.jeecg.modules.user.mapper.FdUserFeedbackMapper;
import org.jeecg.modules.user.service.IFdUserFeedbackService;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.FeedbackAdminVO;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class FdUserFeedbackServiceImpl extends ServiceImpl<FdUserFeedbackMapper, FdUserFeedback>
        implements IFdUserFeedbackService {

    private static final List<String> ALLOWED_TYPES = List.of(
            "功能建议", "性能问题", "内容报错", "积分/签到", "其他问题"
    );

    @Resource
    private IFdUserService userService;

    @Override
    public void submitFeedback(UserInfoVO user, FeedbackSubmitDTO dto) {
        if (user == null || user.getId() == null) {
            throw new JeecgBootException("用户未登录");
        }
        if (!ALLOWED_TYPES.contains(dto.getFeedbackType())) {
            throw new JeecgBootException("反馈类型不正确");
        }
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (oConvertUtils.isEmpty(content)) {
            throw new JeecgBootException("反馈内容不能为空");
        }

        List<String> images = dto.getImages() == null ? Collections.emptyList() : dto.getImages();
        if (images.size() > 3) {
            throw new JeecgBootException("最多上传3张图片");
        }

        FdUserFeedback feedback = new FdUserFeedback();
        feedback.setUserId(user.getId());
        feedback.setFeedbackType(dto.getFeedbackType());
        feedback.setContent(content);
        feedback.setContact(oConvertUtils.isEmpty(dto.getContact()) ? null : dto.getContact().trim());
        feedback.setImages(images.isEmpty() ? null : JSON.toJSONString(images));
        feedback.setStatus(0);
        feedback.setCreateTime(System.currentTimeMillis());
        save(feedback);
    }

    @Override
    public IPage<FeedbackAdminVO> pageForAdmin(FdUserFeedback query, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdUserFeedback> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (oConvertUtils.isNotEmpty(query.getFeedbackType())) {
                wrapper.eq(FdUserFeedback::getFeedbackType, query.getFeedbackType());
            }
            if (query.getStatus() != null) {
                wrapper.eq(FdUserFeedback::getStatus, query.getStatus());
            }
            if (query.getUserId() != null) {
                wrapper.eq(FdUserFeedback::getUserId, query.getUserId());
            }
        }
        wrapper.orderByDesc(FdUserFeedback::getCreateTime);
        IPage<FdUserFeedback> entityPage = page(new Page<>(pageNo, pageSize), wrapper);
        return entityPage.convert(this::toAdminVO);
    }

    @Override
    public FeedbackAdminVO getAdminDetail(Long id) {
        FdUserFeedback feedback = getById(id);
        if (feedback == null) {
            throw new JeecgBootException("反馈记录不存在");
        }
        return toAdminVO(feedback);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        FdUserFeedback feedback = getById(id);
        if (feedback == null) {
            throw new JeecgBootException("反馈记录不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new JeecgBootException("状态值不正确");
        }
        feedback.setStatus(status);
        updateById(feedback);
    }

    private FeedbackAdminVO toAdminVO(FdUserFeedback feedback) {
        FeedbackAdminVO vo = new FeedbackAdminVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setFeedbackType(feedback.getFeedbackType());
        vo.setContent(feedback.getContent());
        vo.setContact(feedback.getContact());
        vo.setStatus(feedback.getStatus());
        vo.setCreateTime(feedback.getCreateTime());
        vo.setImages(parseImages(feedback.getImages()));

        if (feedback.getUserId() != null) {
            FdUser user = userService.getById(feedback.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserPhone(user.getPhone());
            }
        }
        return vo;
    }

    private List<String> parseImages(String imagesJson) {
        if (oConvertUtils.isEmpty(imagesJson)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(imagesJson, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
