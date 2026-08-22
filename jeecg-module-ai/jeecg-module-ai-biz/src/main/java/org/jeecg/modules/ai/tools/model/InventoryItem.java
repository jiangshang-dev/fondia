package org.jeecg.modules.ai.tools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    private String name;
    private String amount;
    private String expireDate;
    private Integer daysToExpire;
    private Boolean expiringSoon;
}
