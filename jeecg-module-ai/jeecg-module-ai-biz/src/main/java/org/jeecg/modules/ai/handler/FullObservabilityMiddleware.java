package org.jeecg.modules.ai.handler;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import io.agentscope.core.middleware.ReasoningInput;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 观测中间件：打印每轮大模型调用 / 推理 / 工具执行耗时，便于定位慢点
 * @author xiaoming todo 禁止提交
 */
@Slf4j
public class FullObservabilityMiddleware implements MiddlewareBase {

    private final AtomicInteger modelCallSeq = new AtomicInteger(0);
    private final AtomicInteger reasoningSeq = new AtomicInteger(0);
    private final AtomicInteger actingSeq = new AtomicInteger(0);

    @Override
    public Flux<AgentEvent> onModelCall(Agent agent, RuntimeContext ctx, ModelCallInput input, Function<ModelCallInput, Flux<AgentEvent>> next) {
        int seq = modelCallSeq.incrementAndGet();
        long start = System.currentTimeMillis();
        String sessionId = ctx != null ? ctx.getSessionId() : null;
        log.info("[LLM] 开始调用大模型 modelCall#{} sessionId={}", seq, sessionId);
        return next.apply(input)
                .doOnComplete(() -> log.info("[LLM] 大模型调用结束 modelCall#{} sessionId={} 耗时={}ms", seq, sessionId, System.currentTimeMillis() - start))
                .doOnError(e -> log.warn("[LLM] 大模型调用失败 modelCall#{} sessionId={} 耗时={}ms err={}", seq, sessionId, System.currentTimeMillis() - start, e.toString()))
                .doOnCancel(() -> log.warn("[LLM] 大模型调用取消 modelCall#{} sessionId={} 已耗时={}ms", seq, sessionId, System.currentTimeMillis() - start));
    }

    @Override
    public Flux<AgentEvent> onReasoning(Agent agent, RuntimeContext ctx, ReasoningInput input, Function<ReasoningInput, Flux<AgentEvent>> next) {
        int seq = reasoningSeq.incrementAndGet();
        long start = System.currentTimeMillis();
        String sessionId = ctx != null ? ctx.getSessionId() : null;
        String name = agent != null ? agent.getName() : "-";
        log.info("[LLM] 开始推理阶段 reasoning#{} sessionId={} agent={}", seq, sessionId, name);
        return next.apply(input).doOnComplete(() -> log.info("[LLM] 推理阶段结束 reasoning#{} sessionId={} 耗时={}ms", seq, sessionId, System.currentTimeMillis() - start))
                .doOnError(e -> {
                            log.warn("[LLM] 推理阶段失败 reasoning#{} sessionId={} 耗时={}ms err={}", seq, sessionId, System.currentTimeMillis() - start, e.toString());
                        })
                .doOnCancel(() -> {
                    log.warn("[LLM] 推理阶段取消 reasoning#{} sessionId={} 已耗时={}ms", seq, sessionId, System.currentTimeMillis() - start);
                });
    }

    @Override
    public Flux<AgentEvent> onActing(Agent agent, RuntimeContext ctx, ActingInput input, Function<ActingInput, Flux<AgentEvent>> next) {
        int seq = actingSeq.incrementAndGet();
        long start = System.currentTimeMillis();
        String sessionId = ctx != null ? ctx.getSessionId() : null;
        String tools = summarizeTools(input);
        String name = agent != null ? agent.getName() : "-";
        log.info("[TOOL] 开始执行工具 acting#{} sessionId={} agent={} tools={}", seq, sessionId, name, tools);
        return next.apply(input)
                .doOnComplete(() -> log.info("[TOOL] 工具执行结束 acting#{} sessionId={} tools={} 耗时={}ms", seq, sessionId, tools, System.currentTimeMillis() - start))
                .doOnError(e -> log.warn("[TOOL] 工具执行失败 acting#{} sessionId={} tools={} 耗时={}ms err={}", seq, sessionId, tools, System.currentTimeMillis() - start, e.toString()))
                .doOnCancel(() -> log.warn("[TOOL] 工具执行取消 acting#{} sessionId={} tools={} 已耗时={}ms", seq, sessionId, tools, System.currentTimeMillis() - start));
    }

    private static String summarizeTools(ActingInput input) {
        if (input == null || input.toolCalls() == null || input.toolCalls().isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (Object item : input.toolCalls()) {
            if (!(item instanceof io.agentscope.core.message.ToolUseBlock t)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(t.getName());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }
}
