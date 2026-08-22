package org.jeecg.modules.ai.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fondia.agentscope")
public class AgentScopeProperties {

    private ModelProperties llm = new ModelProperties();
    private String agentName = "assistant";
    private String workspace = "./.fondiaAgent";
    private RedisProperties redis = new RedisProperties();
    private MemoryProperties memory = new MemoryProperties();
    /**
     * 系统提示词在 Nacos 中的 dataId 定位。
     * 正文在独立配置 {@code agent-system-message.txt}，不写进 yaml。
     */
    private System system = new System();

    @Data
    public static class RedisProperties {
        private String keyPrefix = "jeecg-ai:session:";
        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
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

        // 是否启用小模型压缩功能开关
        private Boolean smallModelEnabled = false;
        // 压缩用小模型名称
        private String compactionModelName = "qwen/qwen3-1.7b";
        // 压缩用小模型基础URL
        private String compactionModelBaseUrl = "http://192.168.1.88:1234";
        // 压缩用小模型 API Key
        private String compactionModelApiKey = "sk-d3580966983048d99b53a77fc67e8372";

        // 是否启用多agent（yaml: fondia.agentscope.memory.subagent-eable）
        private boolean subagentEable = false;
    }

    @Data
    public static class System {
        private Chat routeAgent;
        /** 主 Agent 系统提示词，对应 Nacos Data Id：agent-system-message.txt */
        private Chat chat = new Chat();
        private Chat text;

        @Data
        public static class Chat {
            private String dataId = "agent-system-message.txt";
            private String group = "DEFAULT_GROUP";
            private long timeoutMs = 20000L;
        }
    }
}
