package org.jeecg.modules.ai.config;

import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.model.transport.HttpTransportConfig;
import io.agentscope.core.model.transport.HttpVersion;
import io.agentscope.core.model.transport.JdkHttpTransport;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.redis.state.RedisAgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryConfig;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.ai.tools.WeatherTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * AgentScope + Spring 核心 Bean：复用 Jeecg 自带 Redis（StringRedisTemplate）。
 */
@Configuration
@Slf4j
public class AgentScopeConfig {

    /**
     * Agent 状态存储：走框架 Redis，不再单独 new JedisPooled。
     */
    @Bean
    public AgentStateStore agentStateStore(
            StringRedisTemplate stringRedisTemplate, AgentScopeDemoProperties properties) {
        String keyPrefix = properties.getRedis().getKeyPrefix();
        log.info("AgentScope Redis 使用 Jeecg StringRedisTemplate, keyPrefix={}", keyPrefix);
        return RedisAgentStateStore.builder()
                .clientAdapter(new SpringDataRedisClientAdapter(stringRedisTemplate))
                .keyPrefix(keyPrefix)
                .build();
    }

    @Bean
    public Toolkit toolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTool());
        return toolkit;
    }

    @Bean
    public Model chatModel(AgentScopeDemoProperties properties) {
        AgentScopeDemoProperties.ModelProperties llm = properties.getLlm();
        JdkHttpTransport httpTransport =
                JdkHttpTransport.builder()
                        .config(
                                HttpTransportConfig.builder()
                                        .httpVersion(HttpVersion.HTTP_1_1)
                                        .build())
                        .build();
        return OpenAIChatModel.builder()
                .baseUrl(llm.getBaseUrl())
                .apiKey(llm.getApiKey())
                .modelName(llm.getModelName())
                .stream(llm.isStream())
                .httpTransport(httpTransport)
                .build();
    }

    @Bean
    public HarnessAgent harnessAgent(
            Model chatModel,
            AgentStateStore agentStateStore,
            Toolkit toolkit,
            AgentScopeDemoProperties properties) {
        AgentScopeDemoProperties.MemoryProperties memory = properties.getMemory();

        HarnessAgent.Builder builder =
                HarnessAgent.builder()
                        .name(properties.getAgentName())
                        .sysPrompt(properties.getSysPrompt())
                        .model(chatModel)
                        .toolkit(toolkit)
                        .workspace(Path.of(properties.getWorkspace()))
                        .stateStore(agentStateStore)
                        .memory(
                                MemoryConfig.builder()
                                        .flushTrigger(
                                                MemoryConfig.FlushTrigger.throttled(
                                                        Duration.ofMinutes(5)))
                                        .build())
                        .permissionContext(
                                PermissionContextState.builder()
                                        .mode(PermissionMode.BYPASS)
                                        .build());

        if (memory.isCompactionEnabled()) {
            builder.compaction(
                    CompactionConfig.builder()
                            .triggerMessages(memory.getCompactionTriggerMessages())
                            .keepMessages(memory.getCompactionKeepMessages())
                            .build());
            log.info(
                    "LLM 对话压缩已启用 trigger={} keep={}",
                    memory.getCompactionTriggerMessages(),
                    memory.getCompactionKeepMessages());
        } else {
            builder.disableCompaction();
            log.info("LLM 对话压缩已关闭");
        }

        return builder.build();
    }
}
