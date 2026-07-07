package org.jeecg.modules.fasting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fasting.dto.FastAiChatDTO;
import org.jeecg.modules.fasting.entity.FdFastAiMessage;
import org.jeecg.modules.fasting.vo.FastAiChatVO;

public interface IFdFastAiMessageService extends IService<FdFastAiMessage> {
    FastAiChatVO chat(Long userId, FastAiChatDTO dto);
}
