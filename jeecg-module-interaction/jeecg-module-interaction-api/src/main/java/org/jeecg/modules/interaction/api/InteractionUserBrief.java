package org.jeecg.modules.interaction.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户简要信息")
public class InteractionUserBrief {

    private Long id;
    private String nickname;
    private String avatar;
}
