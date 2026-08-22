package org.jeecg.modules.ai.service; // 声明当前类所在的包，便于 Spring 扫描与组织代码

import org.jeecg.modules.ai.dto.AiChatDTO;
import org.jeecg.modules.ai.dto.ChatDtos;
import org.jeecg.modules.ai.dto.resp.SseResponse;
import org.jeecg.modules.ai.vo.AiMessageVO;
import org.jeecg.modules.ai.vo.AiSessionSummaryVO;
import org.jeecg.modules.ai.vo.AiSessionVO;
import org.jeecg.modules.ai.vo.MessageDataVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {


    List<AiSessionSummaryVO> historyList(String authorization, String source, Integer pageNo, Integer pageSize);

    /**
     * 新建AI对话会话
     */
    AiSessionVO createSession(String authorization, String source);

    /** 获取用户当前（最近打开）的 AI 会话，用于 App 恢复聊天页。 */
    AiSessionVO activeSession(String authorization, String source);

    /**
     * AI会话历史消息
     * @param authorization token
     * @param id 会话ID
     * @return 消息列表
     */
    List<AiMessageVO> messagesById(String authorization, String id);

    /**
     * 停止对话
     * @param authorization
     * @param request
     */
    void stopChat(String authorization, ChatDtos.StopChatRequest request);

    Flux<SseResponse<MessageDataVO<Object>>> chat(String authorization, AiChatDTO dto);
}
