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
import java.util.List;

import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.ai.handler.CompactionHintMiddleware;
import org.jeecg.modules.ai.handler.FullObservabilityMiddleware;
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

    @Resource
    private SystemPromptConfig systemPromptConfig;

    /**
     * Agent 状态存储：走框架 Redis，不再单独 new JedisPooled。
     */
    @Bean
    public AgentStateStore agentStateStore(StringRedisTemplate stringRedisTemplate, AgentScopeProperties properties) {
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

    /**
     * 大模型配置
     * @param properties
     * @return
     */
    @Bean
    public Model chatModel(AgentScopeProperties properties) {
        HttpTransportConfig transportConfig = HttpTransportConfig.builder().httpVersion(HttpVersion.HTTP_1_1).build();
        AgentScopeProperties.ModelProperties llm = properties.getLlm();
        JdkHttpTransport httpTransport = JdkHttpTransport.builder().config(transportConfig).build();
        return OpenAIChatModel.builder().baseUrl(llm.getBaseUrl()).apiKey(llm.getApiKey())
                .modelName(llm.getModelName()).stream(llm.isStream()).httpTransport(httpTransport).build();
    }

    @Bean
    public HarnessAgent harnessAgent(Model chatModel, AgentStateStore agentStateStore, Toolkit toolkit, AgentScopeProperties properties) {
        AgentScopeProperties.MemoryProperties memory = properties.getMemory();

        MemoryConfig.FlushTrigger flushTrigger = MemoryConfig.FlushTrigger.throttled(Duration.ofMinutes(5));
        MemoryConfig memoryConfig = MemoryConfig.builder()
                .flushTrigger(flushTrigger).build();

        PermissionContextState permissionContextState = PermissionContextState.builder()
                .mode(PermissionMode.BYPASS).build();

        String systemText = systemPromptConfig.getAgentSystemMessage().get();
        log.info("系统提示词：{}", systemText);
        HarnessAgent.Builder builder = HarnessAgent.builder()
                .name(properties.getAgentName())
                .sysPrompt(systemText)
                .model(chatModel)
                .toolkit(toolkit)
                .workspace(Path.of(properties.getWorkspace()))
                .stateStore(agentStateStore)
                .middlewares(List.of(
                        // 上下文给前端压缩提示
                        new CompactionHintMiddleware(
                                properties.getMemory().getCompactionTriggerMessages(),
                                properties.getMemory().isCompactionEnabled()),
                        new FullObservabilityMiddleware()
                ))
                .memory(memoryConfig)
                .permissionContext(permissionContextState)
                .toolResultEviction(ToolResultEvictionConfig.defaults())
                .disableSubagents();

        CompactionConfig.Builder keptMessages = CompactionConfig.builder()
                .triggerMessages(memory.getCompactionTriggerMessages())
                .keepMessages(memory.getCompactionKeepMessages());

        log.info("是否开启了上下文压缩{}", memory.isCompactionEnabled());
        if (memory.isCompactionEnabled()) {
            Boolean smallModelEnabled = memory.getSmallModelEnabled();
            log.info("开启通过使用小模型压缩: {}", memory.getSmallModelEnabled());
            if (smallModelEnabled) {
                String compactionModelBaseUrl = memory.getCompactionModelBaseUrl();
                String compactionModelName = memory.getCompactionModelName();
                String compactionModelApiKey = memory.getCompactionModelApiKey();

                log.info("小模型压缩名称:{}", compactionModelName);
                log.info("小模型压缩地址:{}", compactionModelBaseUrl);
                log.info("小模型压缩key:{}", compactionModelApiKey);

                Model compactionModel = OpenAIChatModel.builder()
                        .modelName(compactionModelName)
                        .baseUrl(compactionModelBaseUrl)
                        .apiKey(compactionModelApiKey)
                        .build();

                log.info("使用小模型压缩 model={}", compactionModelName);
                keptMessages.model(compactionModel);
            }

            builder.compaction(keptMessages.build());
            log.info("LLM 对话压缩已启用 trigger={} keep={}", memory.getCompactionTriggerMessages(), memory.getCompactionKeepMessages());
        } else {
            builder.disableCompaction();
            log.info("LLM 对话压缩已关闭");
        }

        return builder.build();
    }
}
