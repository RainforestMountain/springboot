package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityPrizeDO extends BaseDO{
    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 活动关联的奖品id
     */
    private Long prizeId;

    /**
     * 奖品数量
     */
    private Long prizeAmount;

    /**
     * 奖品等级
     */
    private String prizeTiers;

    /**
     * 活动奖品状态
     *
     * @See ActivityPrizeStatusEnum#name()
     */
    private String status;
}
