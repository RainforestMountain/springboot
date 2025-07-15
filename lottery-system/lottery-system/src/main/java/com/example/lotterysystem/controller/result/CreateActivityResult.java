package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建活动, 返回结果(data)
 */
@Data
public class CreateActivityResult implements Serializable {
    /**
     * 活动id
     */
    private Long activityId;
}
