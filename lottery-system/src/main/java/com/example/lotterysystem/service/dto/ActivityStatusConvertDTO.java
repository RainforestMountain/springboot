package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * activityStatusConvertDTO statusConvertDTO 是状态转换请求对象，
 * 包含了状态转换所需的所有信息。
 */
@Data
public class ActivityStatusConvertDTO {
    /**
     * 要转换的活动id
     */
    private Long activityId;

    /**
     * 活动目标状态
     */
    private ActivityStatusEnum activityTargetStatus;

    /**
     * 要转换的活动奖品id, 一次抽一种奖品, 一种奖品有多个用户抽中
     */
    private Long prizeId;

    /**
     * 活动奖品目标状态
     */
    private ActivityPrizeStatusEnum prizeTargetStatus;

    /**
     * 要转换的活动用户id列表
     */
    private List<Long> UserIds;

    /**
     * 活动用户目标状态
     */
    private ActivityUserStatusEnum userTargetStatus;
}
