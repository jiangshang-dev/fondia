package org.jeecg.modules.interaction.moderation;

public interface IContentModerationService {

    ModerationResult moderate(String content);
}
