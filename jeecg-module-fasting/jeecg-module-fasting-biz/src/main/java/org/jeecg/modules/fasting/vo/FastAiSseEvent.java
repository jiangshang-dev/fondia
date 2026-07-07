package org.jeecg.modules.fasting.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastAiSseEvent {
    /** delta | done | error */
    private String type;
    private String content;
    private String sessionId;
    private String message;
}
