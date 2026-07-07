package org.jeecg.modules.fasting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.fasting.dto.FastAiChatDTO;
import org.jeecg.modules.fasting.entity.FdFastAiMessage;
import org.jeecg.modules.fasting.mapper.FdFastAiMessageMapper;
import org.jeecg.modules.fasting.service.IFdFastAiMessageService;
import org.jeecg.modules.fasting.vo.FastAiChatVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class FdFastAiMessageServiceImpl extends ServiceImpl<FdFastAiMessageMapper, FdFastAiMessage>
        implements IFdFastAiMessageService {

    @Override
    public FastAiChatVO chat(Long userId, FastAiChatDTO dto) {
        if (userId == null) {
            throw new JeecgBootException("请先登录后使用 AI 助手");
        }
        String sessionId = oConvertUtils.isNotEmpty(dto.getSessionId())
                ? dto.getSessionId()
                : UUID.randomUUID().toString().replace("-", "");

        saveMessage(userId, sessionId, "user", dto.getQuestion());
        String answer = buildAnswer(dto);
        saveMessage(userId, sessionId, "assistant", answer);

        FastAiChatVO vo = new FastAiChatVO();
        vo.setSessionId(sessionId);
        vo.setAnswer(answer);
        return vo;
    }

    private void saveMessage(Long userId, String sessionId, String role, String content) {
        FdFastAiMessage message = new FdFastAiMessage();
        message.setUserId(userId);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(new Date());
        save(message);
    }

    private String buildAnswer(FastAiChatDTO dto) {
        String question = dto.getQuestion() == null ? "" : dto.getQuestion();
        if (question.contains("咖啡") || question.contains("茶")) {
            return "断食期间可以喝黑咖啡或无糖茶，不要加奶和糖。如有不适请停止断食并咨询医生。";
        }
        if (question.contains("16") || question.contains("计划") || question.contains("怎么选")) {
            return "新手建议从 14:10 或 16:8 开始，适应后再尝试 18:6。关键是进食窗口固定、循序渐进。";
        }
        if (question.contains("头晕") || question.contains("不适")) {
            return "轻微饥饿感较常见。若头晕、心慌明显，请先补充水分和电解质，必要时结束断食并就医。";
        }
        return "坚持你的 " + (oConvertUtils.isNotEmpty(dto.getPlanCode()) ? dto.getPlanCode() : "16_8")
                + " 计划，当前处于 " + (oConvertUtils.isNotEmpty(dto.getPhase()) ? dto.getPhase() : "断食")
                + " 阶段。有问题可继续问我，回答仅供参考。";
    }
}
