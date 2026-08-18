package org.jeecg.modules.ai.service;

import io.agentscope.core.message.ContentBlock;
import org.jeecg.modules.ai.vo.MessageDataVO;
import org.jeecg.modules.ai.vo.ThinkingStepVO;

import java.util.List;
import java.util.Map;

public interface ToolThinkingService {
    String extractToolResultText(ContentBlock data);
    void appendToolResultDelta(Map<String, StringBuilder> toolResultBuffers, String toolCallId, String delta);
    String resolveToolResult(Map<String, StringBuilder> toolResultBuffers);
    MessageDataVO<Object> buildToolStreamData(String streamEvent, String toolCallId, String toolCallName, String message);
    void upsertToolStep(List<ThinkingStepVO> thinkingSteps, Map<String, ThinkingStepVO> toolStepIndex, String toolCallId, String toolCallName, String inputDelta, String content, String status);
}
