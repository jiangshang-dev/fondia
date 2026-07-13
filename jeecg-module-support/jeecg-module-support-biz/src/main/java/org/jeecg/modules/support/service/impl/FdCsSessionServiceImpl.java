package org.jeecg.modules.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
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
import org.jeecg.modules.support.service.CsAiContextBuilder;
import org.jeecg.modules.support.service.IAiRecipeMatchService;
import org.jeecg.modules.support.service.ICsAiService;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.service.IFdCsMessageService;
import org.jeecg.modules.support.service.IFdCsSessionService;
import org.jeecg.modules.support.service.impl.FdCsMessageServiceImpl;
import org.jeecg.modules.support.tool.SearchTool;
import org.jeecg.modules.support.vo.AiChatVO;
import org.jeecg.modules.support.vo.AiRecipeCardVO;
import org.jeecg.modules.support.vo.AiSessionSummaryVO;
import org.jeecg.modules.support.vo.CsMessageVO;
import org.jeecg.modules.support.vo.CsSessionAdminVO;
import org.jeecg.modules.support.vo.CsSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static org.jeecg.modules.support.config.CsAiAsyncExecutorConfig.CS_AI_EXECUTOR;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FdCsSessionServiceImpl extends ServiceImpl<FdCsSessionMapper, FdCsSession> implements IFdCsSessionService {

    @Resource
    private IFdUserService fdUserService;

    @Resource
    private IFdCsConfigService configService;

    @Resource
    private ICsAiService aiService;

    @Resource
    private IFdCsMessageService messageService;

    @Resource
    private FdCsMessageMapper messageMapper;

    @Resource
    private IAiRecipeMatchService recipeMatchService;

    @Resource
    @Qualifier(CS_AI_EXECUTOR)
    private Executor csAiTaskExecutor;

    public static final String AI_TAB_SOURCE = "ai_tab";
    public static final String CS_GENERAL_SOURCE = "general";

    @Override
    public Long resolveUserId(String authorization) {
        UserInfoVO userInfo = fdUserService.getUserInfo(authorization);
        if (userInfo == null || userInfo.getId() == null) {
            throw new JeecgBootException("用户未登录或Token无效");
        }
        return userInfo.getId();
    }

    @Override
    public CsSessionVO getOrCreateAiSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        String channel = oConvertUtils.isEmpty(source) ? AI_TAB_SOURCE : source;
        FdCsSession session = findActiveSession(userId, channel);
        if (session == null) {
            session = createSession(userId, channel, CsSessionStatus.AI);
        }
        return toSessionVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatVO chatWithAi(String authorization, AiChatDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsConfig config = configService.getConfigEntity();
        FdCsSession session = resolveSession(userId, dto.getSessionId(), dto.getSource());

        List<FdCsMessage> history = listRecentMessages(session.getId());
        messageService.saveMessage(session.getId(), CsSenderType.USER, String.valueOf(userId), dto.getContent(), null);

        if (shouldTransfer(dto.getContent(), config.getTransferKeywords())) {
            return doTransfer(session, config);
        }

        String aiInput = CsAiContextBuilder.buildPrompt(history, dto.getContent());
        ICsAiService.AiChatResult aiResult = aiService.chat(
                config.getAiAppId(),
                aiInput,
                session.getAiConversationId(),
                authorization,
                userId
        );

        session.setAiConversationId(aiResult.conversationId());
        session.setLastMessage(trimSummary(aiResult.reply()));
        session.setLastMessageTime(System.currentTimeMillis());
        updateById(session);

        List<AiRecipeCardVO> recipes = recipeMatchService.matchForChat(
                session.getId(), dto.getContent(), aiResult.reply());

        FdCsMessage aiMsg = messageService.saveMessage(
                session.getId(), CsSenderType.AI, "AI", aiResult.reply(), null, recipes);

        AiChatVO vo = new AiChatVO();
        vo.setSessionId(String.valueOf(session.getId()));
        vo.setReply(aiMsg.getContent());
        vo.setTransferred(false);
        vo.setRecipes(recipes);
        return vo;
    }

    @Override
    public SseEmitter chatWithAiStream(String authorization, AiChatDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsConfig config = configService.getConfigEntity();
        FdCsSession session = resolveSession(userId, dto.getSessionId(), dto.getSource());

        List<FdCsMessage> history = listRecentMessages(session.getId());
        messageService.saveMessage(session.getId(), CsSenderType.USER, String.valueOf(userId), dto.getContent(), null);

        SseEmitter emitter = new SseEmitter(0L);

        // 主线程提前捕获，异步内禁止访问 Shiro / RequestContextHolder
        final Long sessionId = session.getId();
        final String aiAppId = config.getAiAppId();
        final String transferKeywords = config.getTransferKeywords();
        final String aiConversationId = session.getAiConversationId();
        final String userMessage = dto.getContent();

        if (shouldTransfer(userMessage, transferKeywords)) {
            csAiTaskExecutor.execute(() -> pushTransferStream(emitter, sessionId, config));
            return emitter;
        }

        final String aiInput = CsAiContextBuilder.buildPrompt(history, userMessage);
        csAiTaskExecutor.execute(() -> runAiStreamTask(
                emitter, sessionId, aiAppId, aiInput, aiConversationId, authorization, userId, userMessage));

        return emitter;
    }

    /**
     * AI 流式任务：仅使用入参，不访问 SecurityUtils / HttpServletRequest。
     */
    private void runAiStreamTask(SseEmitter emitter, Long sessionId, String aiAppId, String aiInput,
                                 String aiConversationId, String authorization, Long userId, String userMessage) {
        try {
            ICsAiService.StreamChatResult streamResult = aiService.streamChat(
                    aiAppId,
                    aiInput,
                    aiConversationId,
                    authorization,
                    userId,
                    payload -> {
                        try {
                            emitter.send(SseEmitter.event().data(payload));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            FdCsSession sessionRef = getById(sessionId);
            if (sessionRef == null) {
                throw new JeecgBootException("会话不存在");
            }
            sessionRef.setAiConversationId(streamResult.conversationId());
            String reply = streamResult.reply();
            if (oConvertUtils.isEmpty(reply)) {
                reply = "抱歉，我暂时无法回答这个问题，您可以尝试换个问法或转接人工客服。";
            }
            sessionRef.setLastMessage(trimSummary(reply));
            sessionRef.setLastMessageTime(System.currentTimeMillis());
            updateById(sessionRef);

            List<AiRecipeCardVO> toolRecipes = enrichRecipeUrls(streamResult.toolRecipes());
            if (!toolRecipes.isEmpty()) {
                pushRecipeToolLink(emitter, sessionId, toolRecipes);
            }

            List<AiRecipeCardVO> recipes = mergeRecipeLists(
                    toolRecipes,
                    enrichRecipeUrls(recipeMatchService.matchForChat(sessionId, userMessage, reply)));
            messageService.saveMessage(sessionId, CsSenderType.AI, "AI", reply, null, recipes);

            JSONObject meta = new JSONObject();
            meta.put("event", "APP_META");
            meta.put("sessionId", String.valueOf(sessionId));
            meta.put("recipes", recipes);
            meta.put("transferred", false);
            emitter.send(SseEmitter.event().data(meta.toJSONString()));
            emitter.complete();
        } catch (Exception e) {
            log.error("[CsAiStream] 流式聊天失败 sessionId={}", sessionId, e);
            sendStreamError(emitter, e);
        }
    }

    private void pushTransferStream(SseEmitter emitter, Long sessionId, FdCsConfig config) {
        try {
            FdCsSession session = getById(sessionId);
            if (session == null) {
                throw new JeecgBootException("会话不存在");
            }
            AiChatVO vo = doTransfer(session, config);
            JSONObject meta = new JSONObject();
            meta.put("event", "APP_TRANSFER");
            meta.put("sessionId", vo.getSessionId());
            meta.put("reply", vo.getReply());
            meta.put("humanGreeting", vo.getHumanGreeting());
            meta.put("transferred", true);
            emitter.send(SseEmitter.event().data(meta.toJSONString()));
            emitter.complete();
        } catch (Exception e) {
            log.error("[CsAiStream] 转人工流式失败 sessionId={}", sessionId, e);
            sendStreamError(emitter, e);
        }
    }

    /**
     * AI 文本流结束后、APP_META 结束前，推送菜谱 Tool 结果链接（对齐 ruiyada chunkLink）。
     */
    private void pushRecipeToolLink(SseEmitter emitter, Long sessionId, List<AiRecipeCardVO> toolRecipes)
            throws IOException {
        JSONObject link = new JSONObject();
        link.put("event", "APP_LINK");
        link.put("sessionId", String.valueOf(sessionId));
        link.put("linkType", "recipe");
        link.put("recipes", toolRecipes);
        link.put("message", com.alibaba.fastjson.JSON.toJSONString(toolRecipes));
        emitter.send(SseEmitter.event().data(link.toJSONString()));
        log.info("[CsAiStream] 推送菜谱 Tool 链接 sessionId={} count={}", sessionId, toolRecipes.size());
    }

    private List<AiRecipeCardVO> enrichRecipeUrls(List<AiRecipeCardVO> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<AiRecipeCardVO> enriched = new ArrayList<>();
        for (AiRecipeCardVO card : source) {
            if (card == null || oConvertUtils.isEmpty(card.getId())) {
                continue;
            }
            if (oConvertUtils.isEmpty(card.getUrl())) {
                card.setUrl(SearchTool.buildRecipeUrl(card.getId()));
            }
            enriched.add(card);
        }
        return enriched;
    }

    private List<AiRecipeCardVO> mergeRecipeLists(List<AiRecipeCardVO> primary, List<AiRecipeCardVO> secondary) {
        Map<String, AiRecipeCardVO> merged = new LinkedHashMap<>();
        if (primary != null) {
            for (AiRecipeCardVO card : primary) {
                if (card != null && oConvertUtils.isNotEmpty(card.getId())) {
                    merged.put(card.getId(), card);
                }
            }
        }
        if (secondary != null) {
            for (AiRecipeCardVO card : secondary) {
                if (card != null && oConvertUtils.isNotEmpty(card.getId())) {
                    merged.putIfAbsent(card.getId(), card);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private void sendStreamError(SseEmitter emitter, Exception e) {
        try {
            JSONObject err = new JSONObject();
            err.put("event", "APP_ERROR");
            err.put("message", e.getMessage() == null ? "AI服务异常" : e.getMessage());
            emitter.send(SseEmitter.event().data(err.toJSONString()));
            emitter.complete();
        } catch (IOException ex) {
            log.warn("[CsAiStream] 发送错误事件失败", ex);
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsSessionVO transferToHuman(String authorization, SessionCreateDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsConfig config = configService.getConfigEntity();
        String channel = resolveChannelSource(dto != null ? dto.getSource() : null);
        FdCsSession session = findActiveSession(userId, channel);
        if (session != null && AI_TAB_SOURCE.equals(session.getSource())) {
            session = null;
        }
        if (session == null) {
            session = createSession(userId, channel, CsSessionStatus.WAITING);
        } else {
            session.setStatus(CsSessionStatus.WAITING);
            updateById(session);
        }
        insertHumanGreeting(session, config);
        return toSessionVO(session, config.getHumanGreeting());
    }

    @Override
    public CsSessionVO getActiveSession(String authorization) {
        return getActiveSession(authorization, CS_GENERAL_SOURCE);
    }

    @Override
    public CsSessionVO getActiveSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        String channel = resolveChannelSource(source);
        FdCsSession session = findActiveSession(userId, channel);
        if (session == null) {
            return null;
        }
        FdCsConfig config = configService.getConfigEntity();
        return toSessionVO(session, config.getHumanGreeting());
    }

    @Override
    public List<AiSessionSummaryVO> listAiSessions(String authorization, String source, Integer pageNo, Integer pageSize) {
        Long userId = resolveUserId(authorization);
        String src = oConvertUtils.isEmpty(source) ? AI_TAB_SOURCE : source;
        Page<FdCsSession> page = new Page<>(pageNo, pageSize);
        IPage<FdCsSession> result = page(page, new LambdaQueryWrapper<FdCsSession>()
                .eq(FdCsSession::getUserId, userId)
                .eq(FdCsSession::getSource, src)
                .orderByDesc(FdCsSession::getLastMessageTime));
        return result.getRecords().stream().map(this::toAiSessionSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsSessionVO createAiSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        String src = oConvertUtils.isEmpty(source) ? AI_TAB_SOURCE : source;
        FdCsSession active = findActiveSession(userId, src);
        if (active != null) {
            active.setStatus(CsSessionStatus.CLOSED);
            active.setCloseTime(System.currentTimeMillis());
            updateById(active);
        }
        FdCsSession session = createSession(userId, src, CsSessionStatus.AI);
        return toSessionVO(session);
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

    private List<FdCsMessage> listRecentMessages(Long sessionId) {
        if (sessionId == null) {
            return List.of();
        }
        return messageMapper.selectList(new LambdaQueryWrapper<FdCsMessage>()
                .eq(FdCsMessage::getSessionId, sessionId)
                .in(FdCsMessage::getSenderType, CsSenderType.USER, CsSenderType.AI)
                .orderByAsc(FdCsMessage::getCreateTime)
                .last("LIMIT 24"));
    }

    private FdCsSession resolveSession(Long userId, String sessionId, String source) {
        String channel = resolveChannelSource(source);
        if (oConvertUtils.isNotEmpty(sessionId)) {
            FdCsSession session = getById(Long.valueOf(sessionId));
            if (session != null && userId.equals(session.getUserId())
                    && !CsSessionStatus.CLOSED.equals(session.getStatus())
                    && isSessionInChannel(session, channel)) {
                return session;
            }
        }
        FdCsSession active = findActiveSession(userId, channel);
        if (active != null) {
            return active;
        }
        return createSession(userId, channel, CsSessionStatus.AI);
    }

    private String resolveChannelSource(String source) {
        if (AI_TAB_SOURCE.equals(source)) {
            return AI_TAB_SOURCE;
        }
        if (oConvertUtils.isEmpty(source)) {
            return CS_GENERAL_SOURCE;
        }
        return source;
    }

    private boolean isSessionInChannel(FdCsSession session, String channel) {
        if (AI_TAB_SOURCE.equals(channel)) {
            return AI_TAB_SOURCE.equals(session.getSource());
        }
        if (AI_TAB_SOURCE.equals(session.getSource())) {
            return false;
        }
        if (CS_GENERAL_SOURCE.equals(channel)) {
            return true;
        }
        return channel.equals(session.getSource());
    }

    private FdCsSession findActiveSession(Long userId, String channel) {
        LambdaQueryWrapper<FdCsSession> wrapper = new LambdaQueryWrapper<FdCsSession>()
                .eq(FdCsSession::getUserId, userId)
                .in(FdCsSession::getStatus,
                        CsSessionStatus.AI, CsSessionStatus.WAITING, CsSessionStatus.CHATTING)
                .orderByDesc(FdCsSession::getLastMessageTime)
                .last("LIMIT 1");
        if (AI_TAB_SOURCE.equals(channel)) {
            wrapper.eq(FdCsSession::getSource, AI_TAB_SOURCE);
        } else if (CS_GENERAL_SOURCE.equals(channel)) {
            wrapper.ne(FdCsSession::getSource, AI_TAB_SOURCE);
        } else {
            wrapper.eq(FdCsSession::getSource, channel);
        }
        return getOne(wrapper);
    }

    private FdCsSession createSession(Long userId, String source, String status) {
        long now = System.currentTimeMillis();
        FdCsSession session = new FdCsSession()
                .setUserId(userId)
                .setStatus(status)
                .setSource(oConvertUtils.isEmpty(source) ? CS_GENERAL_SOURCE : source)
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
        vo.setRecipes(FdCsMessageServiceImpl.parseRecipesFromExtra(message.getExtraJson()));
        return vo;
    }

    private AiSessionSummaryVO toAiSessionSummary(FdCsSession session) {
        AiSessionSummaryVO vo = new AiSessionSummaryVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageTime(session.getLastMessageTime());
        vo.setSource(session.getSource());
        return vo;
    }
}
