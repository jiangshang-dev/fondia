package org.jeecg.modules.support.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.support.dto.AiChatDTO;
import org.jeecg.modules.support.dto.SendMessageDTO;
import org.jeecg.modules.support.dto.SessionCreateDTO;
import org.jeecg.modules.support.entity.FdCsSession;
import org.jeecg.modules.support.vo.AiChatVO;
import org.jeecg.modules.support.vo.CsMessageVO;
import org.jeecg.modules.support.vo.CsSessionAdminVO;
import org.jeecg.modules.support.vo.CsSessionVO;

import java.util.List;

public interface IFdCsSessionService extends IService<FdCsSession> {

    Long resolveUserId(String authorization);

    CsSessionVO getOrCreateAiSession(String authorization, String source);

    AiChatVO chatWithAi(String authorization, AiChatDTO dto);

    CsSessionVO transferToHuman(String authorization, SessionCreateDTO dto);

    CsSessionVO getActiveSession(String authorization);

    List<CsMessageVO> listMessages(String authorization, Long sessionId, Integer pageNo, Integer pageSize);

    List<CsMessageVO> listMessagesForAdmin(Long sessionId, Integer pageNo, Integer pageSize);

    CsMessageVO sendUserMessage(String authorization, Long sessionId, SendMessageDTO dto);

    CsMessageVO sendAgentMessage(Long sessionId, String agentId, SendMessageDTO dto);

    void closeSession(String authorization, Long sessionId);

    IPage<CsSessionAdminVO> pageForAdmin(FdCsSession query, Integer pageNo, Integer pageSize);

    void acceptSession(String sessionId, String agentId);

    void adminCloseSession(String sessionId);
}
