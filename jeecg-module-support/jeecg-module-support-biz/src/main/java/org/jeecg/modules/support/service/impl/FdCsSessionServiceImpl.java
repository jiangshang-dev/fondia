package org.jeecg.modules.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.api.CsUserFeignClient;
import org.jeecg.modules.support.dto.AiChatDTO;
import org.jeecg.modules.support.dto.SendMessageDTO;
import org.jeecg.modules.support.dto.SessionCreateDTO;
import org.jeecg.modules.support.entity.FdCsConfig;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.entity.FdCsSession;
import org.jeecg.modules.support.enums.CsSenderType;
import org.jeecg.modules.support.enums.CsSessionStatus;
import org.jeecg.modules.support.mapper.FdCsMessageMapper;
import org.jeecg.modules.support.mapper.FdCsSessionMapper;
import org.jeecg.modules.support.service.ICsAiService;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.service.IFdCsMessageService;
import org.jeecg.modules.support.service.IFdCsSessionService;
import org.jeecg.modules.support.vo.AiChatVO;
import org.jeecg.modules.support.vo.CsMessageVO;
import org.jeecg.modules.support.vo.CsSessionAdminVO;
import org.jeecg.modules.support.vo.CsSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FdCsSessionServiceImpl extends ServiceImpl<FdCsSessionMapper, FdCsSession> implements IFdCsSessionService {

    @Resource
    private CsUserFeignClient userFeignClient;

    @Resource
    private IFdCsConfigService configService;

    @Resource
    private ICsAiService aiService;

    @Resource
    private IFdCsMessageService messageService;

    @Resource
    private FdCsMessageMapper messageMapper;

    @Override
    public Long resolveUserId(String authorization) {
        Result<Map<String, Object>> result = userFeignClient.getUserInfo(authorization);
        if (result == null || !result.isSuccess() || result.getResult() == null) {
            throw new JeecgBootException("用户未登录或Token无效");
        }
        Object id = result.getResult().get("id");
        if (id == null) {
            throw new JeecgBootException("无法获取用户信息");
        }
        return Long.valueOf(String.valueOf(id));
    }

    @Override
    public CsSessionVO getOrCreateAiSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = findActiveSession(userId);
        if (session == null) {
            session = createSession(userId, source, CsSessionStatus.AI);
        }
        return toSessionVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatVO chatWithAi(String authorization, AiChatDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsConfig config = configService.getConfigEntity();
        FdCsSession session = resolveSession(userId, dto.getSessionId(), dto.getSource());

        messageService.saveMessage(session.getId(), CsSenderType.USER, String.valueOf(userId), dto.getContent(), null);

        if (shouldTransfer(dto.getContent(), config.getTransferKeywords())) {
            return doTransfer(session, config);
        }

        ICsAiService.AiChatResult aiResult = aiService.chat(
                config.getAiAppId(),
                dto.getContent(),
                session.getAiConversationId()
        );

        session.setAiConversationId(aiResult.conversationId());
        session.setLastMessage(trimSummary(aiResult.reply()));
        session.setLastMessageTime(System.currentTimeMillis());
        updateById(session);

        FdCsMessage aiMsg = messageService.saveMessage(
                session.getId(), CsSenderType.AI, "AI", aiResult.reply(), null);

        AiChatVO vo = new AiChatVO();
        vo.setSessionId(String.valueOf(session.getId()));
        vo.setReply(aiMsg.getContent());
        vo.setTransferred(false);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsSessionVO transferToHuman(String authorization, SessionCreateDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsConfig config = configService.getConfigEntity();
        FdCsSession session = findActiveSession(userId);
        if (session == null) {
            session = createSession(userId, dto != null ? dto.getSource() : "general", CsSessionStatus.WAITING);
        } else {
            session.setStatus(CsSessionStatus.WAITING);
            if (dto != null && oConvertUtils.isNotEmpty(dto.getSource())) {
                session.setSource(dto.getSource());
            }
            updateById(session);
        }
        insertHumanGreeting(session, config);
        return toSessionVO(session, config.getHumanGreeting());
    }

    @Override
    public CsSessionVO getActiveSession(String authorization) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = findActiveSession(userId);
        if (session == null) {
            return null;
        }
        FdCsConfig config = configService.getConfigEntity();
        return toSessionVO(session, config.getHumanGreeting());
    }

    @Override
    public List<CsMessageVO> listMessages(String authorization, Long sessionId, Integer pageNo, Integer pageSize) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在");
        }
        return queryMessages(sessionId, pageNo, pageSize);
    }

    @Override
    public List<CsMessageVO> listMessagesForAdmin(Long sessionId, Integer pageNo, Integer pageSize) {
        FdCsSession session = getById(sessionId);
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        return queryMessages(sessionId, pageNo, pageSize);
    }

    private List<CsMessageVO> queryMessages(Long sessionId, Integer pageNo, Integer pageSize) {
        Page<FdCsMessage> page = new Page<>(pageNo, pageSize);
        IPage<FdCsMessage> result = messageMapper.selectPage(page,
                new LambdaQueryWrapper<FdCsMessage>()
                        .eq(FdCsMessage::getSessionId, sessionId)
                        .orderByAsc(FdCsMessage::getCreateTime));
        return result.getRecords().stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageVO sendUserMessage(String authorization, Long sessionId, SendMessageDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在");
        }
        if (CsSessionStatus.CLOSED.equals(session.getStatus())) {
            throw new JeecgBootException("会话已关闭");
        }
        return saveAndDispatchUserMessage(session, userId, dto.getContent(), dto.getClientMsgId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageVO sendAgentMessage(Long sessionId, String agentId, SendMessageDTO dto) {
        FdCsSession session = getById(sessionId);
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        if (CsSessionStatus.CLOSED.equals(session.getStatus())) {
            throw new JeecgBootException("会话已关闭");
        }
        if (session.getAgentId() == null) {
            session.setAgentId(agentId);
        }
        session.setStatus(CsSessionStatus.CHATTING);
        FdCsMessage saved = messageService.saveMessage(
                sessionId, CsSenderType.AGENT, agentId, dto.getContent(), dto.getClientMsgId());
        session.setLastMessage(trimSummary(dto.getContent()));
        session.setLastMessageTime(System.currentTimeMillis());
        updateById(session);
        messageService.pushToUser(saved, sessionId);
        return toMessageVO(saved);
    }

    private CsMessageVO saveAndDispatchUserMessage(FdCsSession session, Long userId, String content, String clientMsgId) {
        FdCsMessage saved = messageService.saveMessage(
                session.getId(), CsSenderType.USER, String.valueOf(userId), content, clientMsgId);
        session.setLastMessage(trimSummary(content));
        session.setLastMessageTime(System.currentTimeMillis());
        if (session.getAgentId() != null) {
            session.setAgentUnread((session.getAgentUnread() == null ? 0 : session.getAgentUnread()) + 1);
            messageService.pushToAgent(saved, session.getAgentId());
        } else {
            messageService.pushToAllAgents(saved, session.getId());
        }
        updateById(session);
        return toMessageVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(String authorization, Long sessionId) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在");
        }
        session.setStatus(CsSessionStatus.CLOSED);
        session.setCloseTime(System.currentTimeMillis());
        updateById(session);
    }

    @Override
    public IPage<CsSessionAdminVO> pageForAdmin(FdCsSession query, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdCsSession> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (oConvertUtils.isNotEmpty(query.getStatus())) {
                wrapper.eq(FdCsSession::getStatus, query.getStatus());
            }
            if (query.getUserId() != null) {
                wrapper.eq(FdCsSession::getUserId, query.getUserId());
            }
        }
        wrapper.orderByDesc(FdCsSession::getLastMessageTime).orderByDesc(FdCsSession::getCreateTime);
        IPage<FdCsSession> page = page(new Page<>(pageNo, pageSize), wrapper);
        return page.convert(this::toAdminVO);
    }

    private CsSessionAdminVO toAdminVO(FdCsSession session) {
        CsSessionAdminVO vo = new CsSessionAdminVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setUserId(String.valueOf(session.getUserId()));
        vo.setAgentId(session.getAgentId());
        vo.setStatus(session.getStatus());
        vo.setSource(session.getSource());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageTime(session.getLastMessageTime());
        vo.setUserUnread(session.getUserUnread());
        vo.setAgentUnread(session.getAgentUnread());
        vo.setCreateTime(session.getCreateTime());
        vo.setCloseTime(session.getCloseTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSession(String sessionId, String agentId) {
        FdCsSession session = getById(Long.valueOf(sessionId));
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        session.setAgentId(agentId);
        session.setStatus(CsSessionStatus.CHATTING);
        session.setAgentUnread(0);
        updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCloseSession(String sessionId) {
        FdCsSession session = getById(Long.valueOf(sessionId));
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        session.setStatus(CsSessionStatus.CLOSED);
        session.setCloseTime(System.currentTimeMillis());
        updateById(session);
    }

    private FdCsSession resolveSession(Long userId, String sessionId, String source) {
        if (oConvertUtils.isNotEmpty(sessionId)) {
            FdCsSession session = getById(Long.valueOf(sessionId));
            if (session != null && userId.equals(session.getUserId())
                    && !CsSessionStatus.CLOSED.equals(session.getStatus())) {
                return session;
            }
        }
        FdCsSession active = findActiveSession(userId);
        if (active != null) {
            return active;
        }
        return createSession(userId, source, CsSessionStatus.AI);
    }

    private FdCsSession findActiveSession(Long userId) {
        return getOne(new LambdaQueryWrapper<FdCsSession>()
                .eq(FdCsSession::getUserId, userId)
                .in(FdCsSession::getStatus,
                        CsSessionStatus.AI, CsSessionStatus.WAITING, CsSessionStatus.CHATTING)
                .orderByDesc(FdCsSession::getCreateTime)
                .last("LIMIT 1"));
    }

    private FdCsSession createSession(Long userId, String source, String status) {
        long now = System.currentTimeMillis();
        FdCsSession session = new FdCsSession()
                .setUserId(userId)
                .setStatus(status)
                .setSource(oConvertUtils.isEmpty(source) ? "general" : source)
                .setUserUnread(0)
                .setAgentUnread(0)
                .setCreateTime(now)
                .setLastMessageTime(now);
        save(session);
        return session;
    }

    private AiChatVO doTransfer(FdCsSession session, FdCsConfig config) {
        session.setStatus(CsSessionStatus.WAITING);
        updateById(session);
        FdCsMessage greeting = insertHumanGreeting(session, config);

        AiChatVO vo = new AiChatVO();
        vo.setSessionId(String.valueOf(session.getId()));
        vo.setTransferred(true);
        vo.setHumanGreeting(greeting.getContent());
        vo.setReply("已为您转接人工客服，请稍候...");
        return vo;
    }

    private FdCsMessage insertHumanGreeting(FdCsSession session, FdCsConfig config) {
        String greeting = config.getHumanGreeting();
        FdCsMessage existing = messageMapper.selectOne(new LambdaQueryWrapper<FdCsMessage>()
                .eq(FdCsMessage::getSessionId, session.getId())
                .eq(FdCsMessage::getSenderType, CsSenderType.SYSTEM)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        return messageService.saveMessage(session.getId(), CsSenderType.SYSTEM, "SYSTEM", greeting, null);
    }

    private boolean shouldTransfer(String content, String keywords) {
        if (oConvertUtils.isEmpty(content) || oConvertUtils.isEmpty(keywords)) {
            return false;
        }
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(oConvertUtils::isNotEmpty)
                .anyMatch(content::contains);
    }

    private String trimSummary(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 200 ? text.substring(0, 200) : text;
    }

    private CsSessionVO toSessionVO(FdCsSession session) {
        return toSessionVO(session, null);
    }

    private CsSessionVO toSessionVO(FdCsSession session, String humanGreeting) {
        CsSessionVO vo = new CsSessionVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setStatus(session.getStatus());
        vo.setSource(session.getSource());
        vo.setHumanGreeting(humanGreeting);
        vo.setCreateTime(session.getCreateTime());
        return vo;
    }

    private CsMessageVO toMessageVO(FdCsMessage message) {
        CsMessageVO vo = new CsMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setId(String.valueOf(message.getId()));
        vo.setSessionId(String.valueOf(message.getSessionId()));
        return vo;
    }
}
