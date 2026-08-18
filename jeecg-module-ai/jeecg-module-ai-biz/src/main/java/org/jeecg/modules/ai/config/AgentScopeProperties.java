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
    private String workspace = "./.agentscope/workspace";
    private RedisProperties redis = new RedisProperties();
    private MemoryProperties memory = new MemoryProperties();
    private System system; // 系统提示语

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
    }

    @Data
    public static class System {
        private Chat routeAgent; // 路由智能体系统提示词
        private Chat chat; // 系统提示语，用于材料相关业务
        private Chat text; // 文本提示语，用于问答回复、润色等文本类型的业务

        @Data
        public static class Chat {
            private String dataId;
            private String group = "DEFAULT_GROUP";
            private long timeoutMs = 20000L; // 读取的超时时间，单位毫秒
        }
    }
}
