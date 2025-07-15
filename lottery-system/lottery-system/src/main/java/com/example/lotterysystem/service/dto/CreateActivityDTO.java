package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateActivityDTO implements Serializable {
    /**
     * 活动id
     */
    private Long activityId;
}
