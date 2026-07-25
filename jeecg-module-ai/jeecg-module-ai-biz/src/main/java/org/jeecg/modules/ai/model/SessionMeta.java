package org.jeecg.modules.ai.model;

import io.agentscope.core.state.State;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话元数据，写入 Redis session_meta，供 /support/ai/sessions 等接口使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionMeta implements State {

    private String id;
    private String userId;
    private String source;
    /** AI | CLOSED */
    private String status;
    private String lastMessage;
    private Long lastMessageTime;
    private Long createTime;
}
