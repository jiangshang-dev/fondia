package org.jeecg.modules.ai.config;

import com.google.common.collect.Lists;
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
import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
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
     *
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
                .toolResultEviction(ToolResultEvictionConfig.defaults());
//                .disableSubagents()

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

        // 启用多Agent
        if (memory.isSubagentEable()) {
            // 菜谱Agent   营养Agent  食材Agent  计划Agent  烹饪Agent  用户Agent
            SubagentDeclaration menuAgent = SubagentDeclaration.builder()
                    .name("menu-agent")
                    .description("""
                            菜谱推荐专家。
                                负责：
                                    1. 根据用户需求搜索菜谱
                                    2. 根据已有食材推荐菜谱
                                    3. 根据菜系、口味、烹饪时间筛选菜谱
                                    4. 根据饮食目标推荐菜谱
                                    5. 查询菜谱详细制作信息
                                    适用场景：
                                    - 今天吃什么
                                    - 推荐几个鸡胸肉做法
                                    - 我有番茄、鸡蛋，能做什么
                                    - 推荐一道川菜
                                    - 30分钟内能做什么
                                    不负责：
                                    - 营养数据计算
                                    - 用户画像分析
                                    - 一周饮食计划
                                    - 实际烹饪指导
                            """)
                    .workspace(Path.of(".fondiaAgent/menu"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .tools(List.of("", "")) // todo 调不同的tool
                    .build();

            SubagentDeclaration pabulumAgent = SubagentDeclaration.builder()
                    .name("nutrition-agent")
                    .description("""
                            营养分析专家。
                               负责：
                               1. 分析菜谱营养成分
                               2. 查询热量、蛋白质、脂肪、碳水等数据
                               3. 判断菜谱是否符合用户饮食目标
                               4. 对多个菜谱进行营养对比
                               5. 对一日三餐进行营养结构分析
                               适用场景：
                               - 这道菜热量高吗
                               - 这道菜适合减脂吗
                               - 哪个菜蛋白质更高
                               - 帮我分析今天的饮食
                               不负责：
                               - 菜谱搜索
                               - 用户画像
                               - 创建饮食计划
                            """)
                    .workspace(Path.of(".fondiaAgent/nutrition"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .tools(List.of("", ""))
                    .build();

            SubagentDeclaration ingredientAgent = SubagentDeclaration.builder()
                    .name("ingredient-agent")
                    .description("""
                            食材管理专家。
                                负责：
                                1. 识别用户提供的食材
                                2. 分析食材适合制作哪些菜
                                3. 分析食材之间的搭配
                                4. 根据库存和保质期推荐优先消耗的食材
                                5. 生成食材采购建议
                                适用场景：
                                - 我有鸡蛋、西红柿、牛肉，能做什么
                                - 冰箱里这些东西快过期了怎么办
                                - 帮我清理冰箱
                                - 还缺哪些食材
                                不负责：
                                - 营养计算
                                - 菜谱最终推荐
                                - 一周饮食计划
                            """)
                    .workspace(Path.of(".fondiaAgent/ingredient"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .tools(List.of("", ""))
                    .build();

            SubagentDeclaration planAgent = SubagentDeclaration.builder()
                    .name("plan-agent")
                    .description("""
                            饮食计划专家。
                                负责：
                                1. 制定一日三餐计划
                                2. 制定一周饮食计划
                                3. 根据用户饮食目标安排菜单
                                4. 控制菜谱重复率
                                5. 根据营养分析结果调整菜单
                                6. 生成一周采购清单
                                适用场景：
                                - 帮我安排明天三餐
                                - 帮我制定一周菜单
                                - 给我做一个家庭一周食谱
                                - 根据已有食材制定计划
                                不负责：
                                - 单独查询菜谱详情
                                - 用户画像查询
                                - 底层营养数据计算
                            """)
                    .workspace(Path.of(".fondiaAgent/plan"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .tools(List.of("", ""))
                    .build();

            SubagentDeclaration cookingAgent = SubagentDeclaration.builder()
                    .name("cooking-agent")
                    .description("烹饪Agent")
                    .workspace(Path.of(".fondiaAgent/cooking"))
                    .workspaceMode(WorkspaceMode.ISOLATED)
                    .model("qwen3-max")
                    .steps(8)
                    .tools(List.of("", ""))
                    .build();

            List<SubagentDeclaration> subagentDeclarationList = Lists.newArrayList();
            subagentDeclarationList.add(menuAgent);
            subagentDeclarationList.add(planAgent);
            subagentDeclarationList.add(ingredientAgent);
            subagentDeclarationList.add(pabulumAgent);
            subagentDeclarationList.add(cookingAgent);
            builder.subagents(subagentDeclarationList);
        }

        return builder.build();
    }
}
