package org.jeecg.modules.ai.dto; // 数据传输对象，前后端 API 契约

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天相关 DTO 集合。
 * 使用 static 内部类避免类文件过多。
 */
public final class ChatDtos {

    private ChatDtos() {} // 工具类构造器私有，禁止实例化

    /** 单条聊天消息（展示用） */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageVo {
        private String role; // user | assistant
        private String content; // 消息正文
    }

    /** 会话列表项 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionVo {
        private String sessionId; // 会话 ID
        private String userId; // 用户 ID
        private String title; // 列表标题（首条用户消息摘要）
        private long updatedAt; // 更新时间戳
    }

    /** 统一 API 响应包装 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private int code; // 0=成功，非 0=失败
        private String message; // 提示信息
        private T data; // 业务数据

        /** 成功响应快捷方法 */
        public static <T> ApiResponse<T> ok(T data) {
            return ApiResponse.<T>builder().code(0).message("success").data(data).build();
        }

        /** 失败响应快捷方法 */
        public static <T> ApiResponse<T> fail(String message) {
            return ApiResponse.<T>builder().code(-1).message(message).build();
        }
    }

    /** 创建会话返回 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionCreateResult {
        private String userId;
        private String sessionId; // 客户端后续请求需携带
    }

    /** 历史查询返回 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryResult {
        private String userId;
        private String sessionId;
        private boolean exists; // 会话是否存在
        /** display | full | context | auto */
        private String source; // 实际使用的数据源
        private boolean compacted; // LLM 上下文是否已被 compaction 压缩
        private String archivePath; // JSONL 完整归档路径（可能 null）
        private List<ChatMessageVo> messages; // 消息列表
    }

    /** POST /api/chat/stop 请求体 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopChatRequest {
        private String userId;
        private String sessionId;
    }
}
