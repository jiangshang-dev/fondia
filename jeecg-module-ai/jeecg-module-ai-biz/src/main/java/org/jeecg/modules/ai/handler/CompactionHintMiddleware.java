package org.jeecg.modules.ai.handler;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventEmitter;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ReasoningInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * 上下文压缩可能很慢且默认不推SSE。在推理前若判断可能触发压缩，
 * 先向思考区推一条提示，避免用户以为卡死。
 */
@Slf4j
@RequiredArgsConstructor
public class CompactionHintMiddleware implements MiddlewareBase {

    public static final String TIP_START = "对话上下文较长，正在压缩整理历史消息（可能需要较长时间），请稍候…\n";
    public static final String TIP_DONE = "上下文整理完成，继续分析中…\n";

    private final int triggerMessages;
    private final boolean enabled;

    @Override
    public Flux<AgentEvent> onReasoning(Agent agent, RuntimeContext ctx, ReasoningInput input, Function<ReasoningInput, Flux<AgentEvent>> next) {
        if (!enabled || triggerMessages <= 0) {
            return next.apply(input);
        }
        int conversationSize = countConversationMessages(input);
        boolean mayCompact = conversationSize >= triggerMessages;
        if (!mayCompact) {
            return next.apply(input);
        }

        AtomicBoolean startTipped = new AtomicBoolean(false);
        AtomicBoolean doneTipped = new AtomicBoolean(false);
        return Flux.deferContextual(cv -> {
            AgentEventEmitter.fromContext(cv).ifPresent(emitter -> {
                if (startTipped.compareAndSet(false, true)) {
                    log.info("[Compaction] 即将/正在压缩上下文, sessionId={}, messages={}, tip推送到思考区", ctx != null ? ctx.getSessionId() : null, conversationSize);
                    emitter.emit(new ThinkingBlockDeltaEvent("compaction-hint", "compaction-hint", TIP_START));
                }
            });
            long start = System.currentTimeMillis();
            // 压缩在 next 内部静默执行；第一条真实事件到来 ≈ 压缩结束、模型开始输出
            return next.apply(input)
                    .doOnNext(ev -> AgentEventEmitter.fromContext(cv).ifPresent(emitter -> {
                        if (doneTipped.compareAndSet(false, true)) {
                            log.info("[Compaction] 压缩阶段结束, sessionId={}, 耗时≈{}ms", ctx != null ? ctx.getSessionId() : null, System.currentTimeMillis() - start);
                            emitter.emit(new ThinkingBlockDeltaEvent("compaction-hint", "compaction-hint-done", TIP_DONE));
                        }
                    }))
                    .doOnError(e -> log.warn("[Compaction] 推理/压缩失败 sessionId={} err={}", ctx != null ? ctx.getSessionId() : null, e.toString()));
        });
    }

    private static int countConversationMessages(ReasoningInput input) {
        List<Msg> messages = input == null ? null : input.messages();
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (Msg msg : messages) {
            if (msg != null && msg.getRole() != MsgRole.SYSTEM) {
                n++;
            }
        }
        return n;
    }
}
