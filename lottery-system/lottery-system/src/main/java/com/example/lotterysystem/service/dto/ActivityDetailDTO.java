package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 活动的完整信息, 与redis的缓存有关
 */
@Data
public class ActivityDetailDTO implements Serializable {

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String desc;

    /**
     * 活动状态
     */
    private ActivityStatusEnum status;

    /**
     * 判断活动是否有效
     *
     * @return
     */
    public Boolean valid() {
        return status.equals(ActivityStatusEnum.RUNNING);
    }

    /**
     * 奖品信息(列表)
     */
    private List<PrizeDetailDTO> prizeDTOList;

    /**
     * 人员列表
     */
    private List<UserDetailDTO> userDTOList;

}
