package org.jeecg.modules.interaction.moderation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModerationResult {

    public enum Outcome {
        PASS, REJECT, PENDING
    }

    private final Outcome outcome;
    private final String reason;

    public static ModerationResult pass() {
        return new ModerationResult(Outcome.PASS, null);
    }

    public static ModerationResult reject(String reason) {
        return new ModerationResult(Outcome.REJECT, reason);
    }

    public static ModerationResult pending(String reason) {
        return new ModerationResult(Outcome.PENDING, reason);
    }

    public boolean isPass() {
        return outcome == Outcome.PASS;
    }

    public boolean isReject() {
        return outcome == Outcome.REJECT;
    }
}
