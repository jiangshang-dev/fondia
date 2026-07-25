package org.jeecg.modules.ai.model; // 领域模型包

import org.jeecg.modules.ai.dto.ChatDtos.ChatMessageVo; // 单条消息 DTO
import io.agentscope.core.state.State; // AgentStateStore 要求实现此标记接口才能持久化
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面向 UI 展示的对话历史，持久化在 Redis 的 display_history key 下。
 * <p>与 AgentState.context 分离的原因：
 * Agent 上下文会被 compaction 压缩成摘要，用户原始问答会「消失」；
 * 本对象只追加、不压缩，保证页面历史完整。
 */
@Data // 生成 getter/setter
@NoArgsConstructor // Jackson 反序列化需要
@AllArgsConstructor // 方便 new DisplayChatHistory(messages)
public class DisplayChatHistory implements State {
    /** 按时间顺序的 user/assistant 消息列表 */
    private List<ChatMessageVo> messages = new ArrayList<>();
}
