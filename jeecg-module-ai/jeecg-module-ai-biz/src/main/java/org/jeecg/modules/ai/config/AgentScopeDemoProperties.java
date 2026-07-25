package org.jeecg.modules.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 绑定 agentscope.demo.* 配置项。
 * Redis 连接复用 Jeecg 框架 spring.redis，此处仅保留 key 前缀。
 */
@Data
@Component
@ConfigurationProperties(prefix = "agentscope.demo")
public class AgentScopeDemoProperties {

    private ModelProperties llm = new ModelProperties();
    private String agentName = "assistant";
    private String sysPrompt = "你是一个友好、专业的 AI 助手。";
    private String workspace = "./.agentscope/workspace";
    private RedisProperties redis = new RedisProperties();
    private MemoryProperties memory = new MemoryProperties();

    @Data
    public static class RedisProperties {
        /** 会话状态 key 前缀，连接信息走 spring.redis */
        private String keyPrefix = "jeecg-ai:session:";
    }

    @Data
    public static class ModelProperties {
        private String baseUrl = "http://192.168.1.62:8000/v1";
        private String modelName = "/data/model/Qwen3.5-35B-A3B-GPTQ-Int4";
        private String apiKey = "EMPTY";
        private boolean stream = true;
    }

    @Data
    public static class MemoryProperties {
        private boolean compactionEnabled = true;
        private int compactionTriggerMessages = 30;
        private int compactionKeepMessages = 10;
    }
}
