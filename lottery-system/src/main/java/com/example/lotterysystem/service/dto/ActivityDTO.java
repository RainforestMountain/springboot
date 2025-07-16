package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ActivityDTO implements Serializable {
    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 活动名
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动状态
     */
    private ActivityStatusEnum status;

    /**
     * 活动是否有效
     *
     * @return
     */
    public Boolean valid() {
        return status == ActivityStatusEnum.RUNNING;
    }
}
