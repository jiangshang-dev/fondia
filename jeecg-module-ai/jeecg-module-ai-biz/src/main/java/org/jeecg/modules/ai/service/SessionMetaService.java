package org.jeecg.modules.ai.service;

import io.agentscope.core.state.AgentStateStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jeecg.modules.ai.model.SessionMeta;
import org.jeecg.modules.ai.vo.AiSessionSummaryVO;
import org.jeecg.modules.ai.vo.AiSessionVO;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 会话元数据：列表 / 活跃会话 / 新建 / 关闭。
 * Agent 对话状态仍由 ChatService + AgentStateStore 管理。
 */
@Service
@RequiredArgsConstructor
public class SessionMetaService {

    public static final String AI_TAB_SOURCE = "ai_tab";
    public static final String STATUS_AI = "AI";
    public static final String STATUS_CLOSED = "CLOSED";

    private static final String SESSION_META_KEY = "session_meta";
    private static final String ACTIVE_KEY_PREFIX = "jeecg-ai:active:";

    private final AgentStateStore agentStateStore;
    private final StringRedisTemplate stringRedisTemplate;

    public AiSessionVO createSession(String userId, String source) {
        String src = normalizeSource(source);
        AiSessionVO active = getActiveSession(userId, src);
        if (active != null) {
            closeSession(userId, active.getId());
        }
        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        SessionMeta meta = SessionMeta.builder()
                .id(sessionId)
                .userId(userId)
                .source(src)
                .status(STATUS_AI)
                .lastMessage(null)
                .lastMessageTime(now)
                .createTime(now)
                .build();
        saveMeta(userId, sessionId, meta);
        setActiveSessionId(userId, src, sessionId);
        return toSessionVO(meta);
    }

    public AiSessionVO getActiveSession(String userId, String source) {
        String src = normalizeSource(source);
        String sessionId = stringRedisTemplate.opsForValue().get(activeKey(userId, src));
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        Optional<SessionMeta> meta = loadMeta(userId, sessionId);
        if (meta.isEmpty() || !STATUS_AI.equals(meta.get().getStatus())) {
            stringRedisTemplate.delete(activeKey(userId, src));
            return null;
        }
        if (!src.equals(meta.get().getSource())) {
            return null;
        }
        return toSessionVO(meta.get());
    }

    public AiSessionVO getOrCreateActive(String userId, String source) {
        AiSessionVO active = getActiveSession(userId, source);
        if (active != null) {
            return active;
        }
        return createSession(userId, source);
    }

    public AiSessionVO resolveForChat(String userId, String sessionId, String source) {
        if (sessionId != null && !sessionId.isBlank()) {
            Optional<SessionMeta> meta = loadMeta(userId, sessionId);
            if (meta.isPresent()) {
                SessionMeta m = meta.get();
                if (!userId.equals(m.getUserId())) {
                    throw new IllegalArgumentException("会话不存在");
                }
                if (STATUS_CLOSED.equals(m.getStatus())) {
                    m.setStatus(STATUS_AI);
                    saveMeta(userId, sessionId, m);
                }
                setActiveSessionId(userId, normalizeSource(m.getSource()), sessionId);
                return toSessionVO(m);
            }
        }
        return getOrCreateActive(userId, source);
    }

    public List<AiSessionSummaryVO> listSessions(String userId, String source, int pageNo, int pageSize) {
        String src = normalizeSource(source);
        int page = Math.max(pageNo, 1);
        int size = Math.max(Math.min(pageSize, 100), 1);
        Set<String> sessionIds = agentStateStore.listSessionIds(userId);
        List<SessionMeta> metas = new ArrayList<>();
        for (String id : sessionIds) {
            loadMeta(userId, id).ifPresent(metas::add);
        }
        return metas.stream()
                .filter(m -> src.equals(normalizeSource(m.getSource())))
                .sorted(Comparator.comparing(
                        (SessionMeta m) -> m.getLastMessageTime() == null ? 0L : m.getLastMessageTime()
                ).reversed())
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public void touchSession(String userId, String sessionId, String lastMessage) {
        Optional<SessionMeta> opt = loadMeta(userId, sessionId);
        SessionMeta meta = opt.orElseGet(() -> SessionMeta.builder()
                .id(sessionId)
                .userId(userId)
                .source(AI_TAB_SOURCE)
                .status(STATUS_AI)
                .createTime(System.currentTimeMillis())
                .build());
        meta.setLastMessage(trimSummary(lastMessage));
        meta.setLastMessageTime(System.currentTimeMillis());
        meta.setStatus(STATUS_AI);
        saveMeta(userId, sessionId, meta);
        setActiveSessionId(userId, normalizeSource(meta.getSource()), sessionId);
    }

    public void ensureMeta(String userId, String sessionId, String source) {
        if (loadMeta(userId, sessionId).isPresent()) {
            return;
        }
        long now = System.currentTimeMillis();
        SessionMeta meta = SessionMeta.builder()
                .id(sessionId)
                .userId(userId)
                .source(normalizeSource(source))
                .status(STATUS_AI)
                .createTime(now)
                .lastMessageTime(now)
                .build();
        saveMeta(userId, sessionId, meta);
        setActiveSessionId(userId, meta.getSource(), sessionId);
    }

    public void closeSession(String userId, String sessionId) {
        Optional<SessionMeta> opt = loadMeta(userId, sessionId);
        if (opt.isEmpty()) {
            return;
        }
        SessionMeta meta = opt.get();
        meta.setStatus(STATUS_CLOSED);
        saveMeta(userId, sessionId, meta);
        String active = stringRedisTemplate.opsForValue().get(activeKey(userId, normalizeSource(meta.getSource())));
        if (sessionId.equals(active)) {
            stringRedisTemplate.delete(activeKey(userId, normalizeSource(meta.getSource())));
        }
    }

    private void saveMeta(String userId, String sessionId, SessionMeta meta) {
        agentStateStore.save(userId, sessionId, SESSION_META_KEY, meta);
    }

    private Optional<SessionMeta> loadMeta(String userId, String sessionId) {
        return agentStateStore.get(userId, sessionId, SESSION_META_KEY, SessionMeta.class);
    }

    private void setActiveSessionId(String userId, String source, String sessionId) {
        stringRedisTemplate.opsForValue().set(activeKey(userId, source), sessionId);
    }

    private static String activeKey(String userId, String source) {
        return ACTIVE_KEY_PREFIX + userId + ":" + source;
    }

    private static String normalizeSource(String source) {
        return source == null || source.isBlank() ? AI_TAB_SOURCE : source.trim();
    }

    private static String trimSummary(String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.length() > 100 ? t.substring(0, 100) + "…" : t;
    }

    private AiSessionVO toSessionVO(SessionMeta meta) {
        AiSessionVO vo = new AiSessionVO();
        vo.setId(meta.getId());
        vo.setStatus(meta.getStatus());
        vo.setSource(meta.getSource());
        vo.setCreateTime(meta.getCreateTime());
        return vo;
    }

    private AiSessionSummaryVO toSummary(SessionMeta meta) {
        AiSessionSummaryVO vo = new AiSessionSummaryVO();
        vo.setId(meta.getId());
        vo.setLastMessage(meta.getLastMessage());
        vo.setLastMessageTime(meta.getLastMessageTime());
        vo.setSource(meta.getSource());
        return vo;
    }
}
