package org.jeecg.modules.fasting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fasting.dto.FastAiChatDTO;
import org.jeecg.modules.fasting.entity.FdFastAiMessage;
import org.jeecg.modules.fasting.vo.FastAiChatVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IFdFastAiMessageService extends IService<FdFastAiMessage> {
    FastAiChatVO chat(Long userId, FastAiChatDTO dto);

    /** 流式 AI 问答（SSE） */
    SseEmitter streamChat(Long userId, FastAiChatDTO dto);
}
