package org.jeecg.modules.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.TextBlock;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.ai.service.ToolThinkingService;
import org.jeecg.modules.ai.vo.MessageDataVO;
import org.jeecg.modules.ai.vo.ThinkingStepVO;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.jeecg.modules.ai.constant.ThinkTypeConstant.TOOL;
import static org.jeecg.modules.ai.constant.ToolStatusConstant.DONE;
import static org.jeecg.modules.ai.constant.ToolStatusConstant.RUNNING;

@Slf4j
@Service
public class ToolThinkingServiceImpl implements ToolThinkingService {

    /**
     * 提取MCP工具返回内容中的文本
     */
    @Override
    public String extractToolResultText(ContentBlock data) {
        if (data instanceof TextBlock textBlock) {
            return textBlock.getText();
        }
        return data == null ? "" : data.toString();
    }

    @Override
    public String resolveToolResult(Map<String, StringBuilder> toolResultBuffers) {
        if (toolResultBuffers == null || toolResultBuffers.isEmpty()) {
            return "";
        }
        // 多工具各自返回 JSON 数组时，用逗号拼接会变成非法 JSON；这里合并成一个数组
        List<Object> merged = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        for (StringBuilder buffer : toolResultBuffers.values()) {
            if (buffer == null || buffer.isEmpty()) {
                continue;
            }
            String raw = buffer.toString().trim();
            if (StrUtil.isBlank(raw)) {
                continue;
            }
            try {
                if (JSONUtil.isTypeJSONArray(raw)) {
                    for (Object item : JSONUtil.parseArray(raw)) {
                        appendUniqueMaterial(merged, seenIds, item);
                    }
                } else if (JSONUtil.isTypeJSON(raw)) {
                    appendUniqueMaterial(merged, seenIds, JSONUtil.parseObj(raw));
                } else {
                    String wrapped = "[" + raw + "]";
                    log.info("wrapped:{}", wrapped);
                    if (JSONUtil.isTypeJSONArray(wrapped)) {
                        for (Object item : JSONUtil.parseArray(wrapped)) {
                            if (item instanceof cn.hutool.json.JSONArray nested) {
                                for (Object nestedItem : nested) {
                                    appendUniqueMaterial(merged, seenIds, nestedItem);
                                }
                            } else {
                                appendUniqueMaterial(merged, seenIds, item);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析工具材料链接失败: {}", StrUtil.sub(raw, 0, 200), e);
                e.printStackTrace();
            }
        }
        return merged.isEmpty() ? "" : JSONUtil.toJsonStr(merged);
    }

    /**
     * 累加MCP工具返回片段 最后的link链接
     */
    @Override
    public void appendToolResultDelta(Map<String, StringBuilder> toolResultBuffers, String toolCallId, String delta) {
        log.info("累加MCP工具返回片段 最后的link链接: {}, toolCallId: {}", toolCallId, delta);
        if (StrUtil.isBlank(toolCallId) || StrUtil.isBlank(delta) || delta.contains("Error")) {
            return;
        }
        boolean typeJSON = JSONUtil.isTypeJSON(delta);
        if (typeJSON && (delta.contains("materialId") || delta.contains("id"))) {
            log.info("{}", typeJSON);
            boolean typeJSONArray = JSONUtil.isTypeJSONArray(delta);
            if (typeJSONArray) {
                StringBuilder computed = toolResultBuffers.computeIfAbsent(toolCallId, key -> new StringBuilder());
                if (computed.indexOf(delta) >= 0) {
                    return;
                }
                // type:link
                computed.append(delta);
            }
        }
    }

    @Override
    public MessageDataVO<Object> buildToolStreamData(String streamEvent, String toolCallId, String toolCallName, String message) {
        log.info("输出内容：{}, 事件:{}", message, streamEvent);
        MessageDataVO<Object> messageData = new MessageDataVO<>();
        messageData.setStreamEvent(streamEvent);
        messageData.setToolCallId(toolCallId);
        messageData.setToolCallName(toolCallName);
        messageData.setMessage(message);
        messageData.setTimestamp(System.currentTimeMillis());
        return messageData;
    }

    /**
     * * 更新或插入工具调用步骤
     */
    @Override
    public void upsertToolStep(List<ThinkingStepVO> thinkingSteps, Map<String, ThinkingStepVO> toolStepIndex, String toolCallId, String toolCallName, String inputDelta, String content, String status) {
        log.info("更新或插入工具调用步骤:{}, content:{}, inputDelta:{}, toolCallName:{}",
                thinkingSteps, content, inputDelta, toolCallName
        );
        if (thinkingSteps == null || toolStepIndex == null) {
            return;
        }
        String id = StrUtil.blankToDefault(toolCallId, "tool-" + System.nanoTime());
        String name = StrUtil.blankToDefault(toolCallName, "工具");
        ThinkingStepVO step = toolStepIndex.get(id);
        if (step == null) {
            thinkingSteps.forEach(s -> {
                if (RUNNING.equals(s.getStatus())) {
                    s.setStatus(DONE);
                }
            });
            step = ThinkingStepVO.builder().id(id).type(TOOL)
                    .title("调用工具 " + name).toolCallName(name)
                    .input("").content(content)
                    .status(status).build();
            thinkingSteps.add(step);
            toolStepIndex.put(id, step);
        } else {
            if (StrUtil.isNotBlank(content)) {
                step.setContent(content);
            }
            step.setStatus(status);
            if (StrUtil.isNotBlank(name)) {
                step.setToolCallName(name);
                step.setTitle("调用工具 " + name);
            }
        }
        if (StrUtil.isNotBlank(inputDelta)) {
            step.setInput(StrUtil.blankToDefault(step.getInput(), "") + inputDelta);
        }
    }

    private void appendUniqueMaterial(List<Object> merged, Set<String> seenIds, Object item) {
        if (item == null) {
            return;
        }
        String id = null;
        if (item instanceof cn.hutool.json.JSONObject obj) {
            Object idVal = obj.get("id");
            if (idVal == null) {
                idVal = obj.get("materialId");
            }
            if (idVal != null) {
                id = String.valueOf(idVal);
            }
        }
        if (StrUtil.isNotBlank(id)) {
            if (!seenIds.add(id)) {
                return;
            }
        }
        merged.add(item);
    }

}
