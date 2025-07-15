package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PrizeDetailDTO extends PrizeDTO{
    /**
     * 奖品等级
     */
    private ActivityPrizeTiersEnum tiers;

    /**
     * 奖品数量
     */
    private Long prizeAmount;

    /**
     * 奖品状态
     */
    private ActivityPrizeStatusEnum status;

    public Boolean valid() {
        return status.equals(ActivityPrizeStatusEnum.INIT);
    }
}
